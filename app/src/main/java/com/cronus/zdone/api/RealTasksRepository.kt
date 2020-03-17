package com.cronus.zdone.api

import com.cronus.zdone.AppExecutors
import com.cronus.zdone.api.model.*
import com.cronus.zdone.home.TaskShowerStrategyProvider
import com.cronus.zdone.home.TasksScreen
import com.dropbox.android.external.store4.*
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.HashSet

@Singleton
class RealTasksRepository @Inject constructor(
    var appExecutors: AppExecutors,
    var zdoneService: ZdoneService,
    val taskShowingStrategyProvider: TaskShowerStrategyProvider
) : TasksRepository {

    private val store: Store<Unit, Tasks>

    init {
        store = StoreBuilder
            .fromNonFlow<Unit, Tasks> { zdoneService.getTaskInfoFlow() }
            .build()
    }
    override suspend fun getTasksFromStore(): Flow<StoreResponse<List<Task>>> =
        getCachedData().mapToSubField(Tasks::tasksToDo)

    override suspend fun getTimeDataFromStore(): Flow<StoreResponse<TimeProgress>> =
        getCachedData().mapToSubField(Tasks::timeProgress)

    private suspend fun getCachedData(): Flow<StoreResponse<Tasks>> = coroutineScope {
        store.stream(StoreRequest.cached(Unit, refresh = false))
    }

    private fun <T> Flow<StoreResponse<Tasks>>.mapToSubField(fieldGetter: Tasks.() -> T): Flow<StoreResponse<T>> {
        return map { extractFieldFromTasks(it, fieldGetter) }
    }


    private fun <T> extractFieldFromTasks(response: StoreResponse<Tasks>, extractField: Tasks.() -> T): StoreResponse<T> {
        return when (response) {
            is StoreResponse.Loading -> StoreResponse.Loading(response.origin)
            is StoreResponse.Data -> StoreResponse.Data(response.value.extractField(), response.origin)
            is StoreResponse.Error -> StoreResponse.Error(response.error, response.origin)
        }
    }

    private val CACHE_REFRESH_TIME = 2 * 60 * 1_000L // 2 mins in millis

    private var cachedData: Tasks? = null
    private var requestId: String? = null
    private var lastRequestTime: Long = 0
    var observers: MutableSet<ObservableEmitter<Tasks>> =
        Collections.synchronizedSet(HashSet<ObservableEmitter<Tasks>>())
    private val dataStream = observe(Observable.create<Tasks> { observer ->
        observers.add(observer)
        observer.setCancellable { observers.remove(observer) }

        val timeElapsed = System.currentTimeMillis() - lastRequestTime
        if (timeElapsed < CACHE_REFRESH_TIME) {
            cachedData?.let { observer.onNext(it) }
        } else {
            refreshTaskData()
        }
    })

    override fun refreshTaskData() {
        fetchData { response ->
            synchronized(observers) {
                // synchronized to avoid issue where observer is added/removed on
                // main thread while we are calling this code on background thread
                cachedData = response
                observers.forEach { it ->
                    it.onNext(response)
                }
            }
        }
    }

    // synchronized for updating requestId atomically
    @Synchronized
    private fun <T> fetchData(responseTransformation: (Tasks) -> T) {
        if (requestId == null) {
            lastRequestTime = System.currentTimeMillis()
            requestId = UUID.randomUUID().toString()
            observe(zdoneService.getTaskInfo().map {
                synchronized(this) {
                    requestId = null
                }
                responseTransformation(it)
            }).subscribe()
        }
    }

    override fun getTasks(): Observable<List<Task>> {
        return dataStream.map { it.tasksToDo }
            .map {
                taskShowingStrategyProvider.getStrategy()
                    .selectTasksToShow(it)
            }
    }

    override fun getTimeData(): Observable<TimeProgress> {
        return dataStream.map { it.timeProgress }
    }

    override fun taskCompleted(taskUpdateInfo: TasksRepository.TaskUpdateInfo): Observable<UpdateDataResponse> {
        return observe(
            zdoneService.updateTask(
                TaskStatusUpdate(
                    id = taskUpdateInfo.id,
                    subtaskId = taskUpdateInfo.subtaskId,
                    update = "complete",
                    service = taskUpdateInfo.service,
                    duration_seconds = taskUpdateInfo.duration_seconds
                )
            )
        )
    }

    override fun deferTask(taskUpdateInfo: TasksRepository.TaskUpdateInfo): Observable<UpdateDataResponse> {
        return observe(
            zdoneService.updateTask(
                TaskStatusUpdate(
                    id = taskUpdateInfo.id,
                    subtaskId = taskUpdateInfo.subtaskId,
                    update = "defer",
                    service = taskUpdateInfo.service,
                    duration_seconds = taskUpdateInfo.duration_seconds
                )
            )
        )
    }

    override fun updateWorkTime(maxWorkMins: Int) {
        observe(zdoneService.updateWorkTime(mapOf("maximum_minutes_per_day" to maxWorkMins))
            .doOnNext { refreshTaskData() }).subscribe()
    }

    override fun flushCache() {
        cachedData = null
    }

    override fun taskIsPreviousDay(task: TasksScreen.DisplayedTask): Boolean {
        return DateTime(lastRequestTime, DateTimeZone.getDefault()).dayOfYear().get() < DateTime.now().dayOfYear().get()
    }

    private fun <T> observe(observable: Observable<T>): Observable<T> {
        return observable
            .subscribeOn(appExecutors.network())
            .observeOn(appExecutors.mainThread())
    }

}
