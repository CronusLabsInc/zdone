package com.cronus.zdone.api

import com.cronus.zdone.AppExecutors
import com.cronus.zdone.api.model.*
import com.cronus.zdone.home.TasksScreen
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.HashSet

@Singleton
class TasksRepositoryImpl @Inject constructor(
    var appExecutors: AppExecutors,
    var zdoneService: ZdoneService
) : TasksRepository {

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
    }

    override fun getTimeData(): Observable<TimeProgress> {
        return dataStream.map { it.timeProgress }
    }

    override fun taskCompleted(task: TasksScreen.DisplayedTask): Observable<UpdateDataResponse> {
        return observe(
            zdoneService.updateTask(
                TaskStatusUpdate(
                    id = task.id,
                    subtaskId = task.subtaskId,
                    update = "complete",
                    service = task.service
                )
            )
        )
    }

    override fun deferTask(task: TasksScreen.DisplayedTask): Observable<UpdateDataResponse> {
        return observe(
            zdoneService.updateTask(
                TaskStatusUpdate(
                    id = task.id,
                    subtaskId = task.subtaskId,
                    update = "defer",
                    service = task.service
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
