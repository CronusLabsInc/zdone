package com.cronus.zdone

import com.cronus.zdone.api.TasksRepository
import com.cronus.zdone.api.model.Task
import com.cronus.zdone.api.model.TimeProgress
import com.cronus.zdone.api.model.UpdateDataResponse
import com.cronus.zdone.home.TasksScreen
import com.dropbox.android.external.store4.ResponseOrigin
import com.dropbox.android.external.store4.StoreResponse
import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class TestTasksRepo : TasksRepository {

    private val tasks = listOf(
        Task("abcd", "Anki", null, "habitica", 15)
    )
    private val timeProgress = TimeProgress(15, 85, 9999)

    override suspend fun getTasksFromStore(): Flow<StoreResponse<List<Task>>> {
        return flowOf(StoreResponse.Data(tasks, ResponseOrigin.Cache))
    }

    override suspend fun getTimeDataFromStore(): Flow<StoreResponse<TimeProgress>> {
        return flowOf(StoreResponse.Data(timeProgress, ResponseOrigin.Cache))
    }

    override fun updateWorkTime(maxWorkMins: Int) {}

    override fun deferTask(taskUpdateInfo: TasksRepository.TaskUpdateInfo): Observable<UpdateDataResponse> {
        return Observable.just(UpdateDataResponse("success"))
    }

    override fun taskCompleted(taskUpdateInfo: TasksRepository.TaskUpdateInfo): Observable<UpdateDataResponse> {
        return Observable.just(UpdateDataResponse("success"))
    }

    override fun taskIsPreviousDay(task: TasksScreen.DisplayedTask): Boolean {
        return false
    }

    override fun getTimeData(): Observable<TimeProgress> {
        return Observable.just(timeProgress)
    }

    override fun getTasks(): Observable<List<Task>> {
        return Observable.just(tasks)
    }

    override fun refreshTaskData() {}

    override fun flushCache() {
        // do nothing
    }

}