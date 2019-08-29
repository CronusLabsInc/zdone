package com.cronus.zdone.api

import com.cronus.zdone.AppExecutors
import com.cronus.zdone.WorkTimeManager
import com.cronus.zdone.api.model.*
import com.cronus.zdone.home.HomeScreen
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TasksRepositoryImpl @Inject constructor(
        var appExecutors: AppExecutors,
        var zdoneService: ZdoneService,
        var workTimeManager: WorkTimeManager
) : TasksRepository {

    private val CACHE_REFRESH_TIME = 2 * 60 * 1_000_000_000L

    var cachedData: Observable<Tasks>? = null
    var lastRequestTime: Long = 0
    var lastWorkTime = -1

    override fun refreshTaskData(): Observable<List<Task>> {
        cachedData = Observable.empty<Tasks>().cacheWithInitialCapacity(1)
        return fetchData(workTimeManager.getDefaultWorkTime()) { response -> response.tasksToDo }
    }

    private fun <T> fetchData(workTimeMins: Int, responseTransformation: (Tasks) -> T): Observable<T> {
        cachedData = observe(zdoneService.getTaskInfo(workTimeMins)).cacheWithInitialCapacity(1)
        lastRequestTime = System.nanoTime()
        lastWorkTime = workTimeMins
        return cachedData!!.map {
            responseTransformation(it)
        }
    }

    override fun getTasks(): Observable<List<Task>> {
        return fetchOrCachedData { response -> response.tasksToDo }
    }

    override fun getTimeData(): Observable<TimeProgress> {
        return fetchOrCachedData { response -> response.timeProgress }
    }

    private fun <T> fetchOrCachedData(responseTransformation: (Tasks) -> T): Observable<T> {
        val currentWorkTime = workTimeManager.getDefaultWorkTime()
        cachedData?.let { cacheData ->
            val timeElapsed = System.nanoTime() - lastRequestTime
            if (timeElapsed < CACHE_REFRESH_TIME && lastWorkTime == currentWorkTime) {
                return cacheData.map { responseTransformation(it) }
            }
        }
        return fetchData(currentWorkTime, responseTransformation)
    }

    override fun taskCompleted(task: HomeScreen.DisplayedTask): Observable<UpdateTaskResponse> {
        return observe(zdoneService.updateTask(
                TaskStatusUpdate(
                        id = task.id,
                        subtaskId = task.subtaskId,
                        update = "complete",
                        service = task.service
                )))
    }

    override fun deferTask(task: HomeScreen.DisplayedTask): Observable<UpdateTaskResponse> {
        return observe(zdoneService.updateTask(
                TaskStatusUpdate(
                        id = task.id,
                        subtaskId = task.subtaskId,
                        update = "defer",
                        service = task.service
                )
        ))
    }

    override fun flushCache() {
        cachedData = null
    }

    private fun <T> observe(observable: Observable<T>): Observable<T> {
        return observable
                .cacheWithInitialCapacity(1)
                .subscribeOn(appExecutors.network())
                .observeOn(appExecutors.mainThread())
    }

}
