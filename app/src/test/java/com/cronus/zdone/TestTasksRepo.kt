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
    private val sucessfulUpdate = UpdateDataResponse("success")

    override suspend fun getTasksFromStore(): Flow<StoreResponse<List<Task>>> {
        return flowOf(StoreResponse.Data(tasks, ResponseOrigin.Cache))
    }

    override suspend fun getTimeDataFromStore(): Flow<StoreResponse<TimeProgress>> {
        return flowOf(StoreResponse.Data(timeProgress, ResponseOrigin.Cache))
    }

    override suspend fun updateTask(taskUpdateInfo: TasksRepository.TaskUpdateInfo): Flow<StoreResponse<UpdateDataResponse>> {
        return flowOf(storeResponseFrom(sucessfulUpdate))
    }

    override suspend fun refreshTaskDataFromStore() { }

    override suspend fun flushCacheFromStore() { }

    override fun areTasksFromPreviousDay(): Boolean {
        return false
    }

    private fun <T> storeResponseFrom(t: T): StoreResponse<T> = StoreResponse.Data(t, ResponseOrigin.Fetcher)

}