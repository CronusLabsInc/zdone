package com.cronus.zdone.api

import com.cronus.zdone.AppExecutors
import com.cronus.zdone.api.TasksRepository.*
import com.cronus.zdone.api.model.*
import com.cronus.zdone.home.TaskShowerStrategyProvider
import com.cronus.zdone.home.TasksScreen
import com.dropbox.android.external.store4.*
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
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

    private val taskInfoStore: Store<Unit, Tasks>
    private val taskUpdateStore: Store<TaskUpdateInfo, UpdateDataResponse>
    private var lastRequestTime: Long = 0

    init {
        taskInfoStore = StoreBuilder
            .fromNonFlow<Unit, Tasks> { zdoneService.getTaskInfoFlow() }
            .build()
        taskUpdateStore = StoreBuilder.fromNonFlow<TaskUpdateInfo, UpdateDataResponse>
        { taskUpdateInfo ->
            zdoneService.updateTaskAsync(
                TaskStatusUpdate(
                    id = taskUpdateInfo.id,
                    subtaskId = taskUpdateInfo.subtaskId,
                    update = taskUpdateInfo.updateType,
                    service = taskUpdateInfo.service,
                    duration_seconds = taskUpdateInfo.duration_seconds
                )
            )
        }
            .build()

    }

    override suspend fun getTasksFromStore(): Flow<StoreResponse<List<Task>>> =
        getCachedData().mapToSubField(Tasks::tasksToDo)

    override suspend fun getTimeDataFromStore(): Flow<StoreResponse<TimeProgress>> =
        getCachedData().mapToSubField(Tasks::timeProgress)

    private suspend fun getCachedData(): Flow<StoreResponse<Tasks>> = coroutineScope {
        lastRequestTime = System.currentTimeMillis()
        taskInfoStore.stream(StoreRequest.cached(Unit, refresh = false))
    }

    private fun <T> Flow<StoreResponse<Tasks>>.mapToSubField(fieldGetter: Tasks.() -> T): Flow<StoreResponse<T>> {
        return map { extractFieldFromTasks(it, fieldGetter) }
    }


    private fun <T> extractFieldFromTasks(
        response: StoreResponse<Tasks>,
        extractField: Tasks.() -> T
    ): StoreResponse<T> {
        return when (response) {
            is StoreResponse.Loading -> StoreResponse.Loading(response.origin)
            is StoreResponse.Data -> StoreResponse.Data(
                response.value.extractField(),
                response.origin
            )
            is StoreResponse.Error -> StoreResponse.Error(response.error, response.origin)
        }
    }

    override suspend fun taskCompletedFromStore(taskUpdateInfo: TaskUpdateInfo): Flow<StoreResponse<UpdateDataResponse>> {
        return updateTask(taskUpdateInfo)
    }

    override suspend fun deferTaskFromStore(taskUpdateInfo: TaskUpdateInfo): Flow<StoreResponse<UpdateDataResponse>> {
        return updateTask(taskUpdateInfo)
    }

    private suspend fun updateTask(taskUpdateInfo: TaskUpdateInfo): Flow<StoreResponse<UpdateDataResponse>> =
        coroutineScope {
            taskUpdateStore.stream(StoreRequest.fresh(taskUpdateInfo))
        }

    override suspend fun refreshTaskDataFromStore() {
        taskInfoStore.fresh(Unit)
    }

    override suspend fun flushCacheFromStore() {
        taskInfoStore.clearAll()
    }

    override fun areTasksFromPreviousDay(): Boolean {
        return DateTime(
            lastRequestTime,
            DateTimeZone.getDefault()
        ).dayOfYear().get() < DateTime.now().dayOfYear().get()
    }

    private fun <T> observe(observable: Observable<T>): Observable<T> {
        return observable
            .subscribeOn(appExecutors.network())
            .observeOn(appExecutors.mainThread())
    }

}
