package com.cronus.zdone.api

import com.cronus.zdone.api.model.Task
import com.cronus.zdone.api.model.TimeProgress
import com.cronus.zdone.api.model.UpdateDataResponse
import com.cronus.zdone.home.TasksScreen
import com.dropbox.android.external.store4.StoreResponse
import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow

interface TasksRepository {

    suspend fun getTasksFromStore(): Flow<StoreResponse<List<Task>>>

    suspend fun getTimeDataFromStore(): Flow<StoreResponse<TimeProgress>>

    suspend fun taskCompletedFromStore(taskUpdateInfo: TaskUpdateInfo): Flow<StoreResponse<UpdateDataResponse>>

    suspend fun deferTaskFromStore(taskUpdateInfo: TaskUpdateInfo): Flow<StoreResponse<UpdateDataResponse>>

    suspend fun refreshTaskDataFromStore()

    suspend fun flushCacheFromStore()

    fun areTasksFromPreviousDay() : Boolean

    data class TaskUpdateInfo(
        val id: String,
        val subtaskId: String?,
        val service: String,
        val duration_seconds: Long?,
        val updateType: String
    )

}
