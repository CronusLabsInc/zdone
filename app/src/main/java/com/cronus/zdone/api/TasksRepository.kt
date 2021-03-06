package com.cronus.zdone.api

import com.cronus.zdone.api.model.AddTaskInfo
import com.cronus.zdone.api.model.Task
import com.cronus.zdone.api.model.TimeProgress
import com.cronus.zdone.api.model.UpdateDataResponse
import com.cronus.zdone.stats.TaskUpdateType
import com.dropbox.android.external.store4.StoreResponse
import kotlinx.coroutines.flow.Flow

interface TasksRepository {

    suspend fun getTasksFromStore(): Flow<StoreResponse<List<Task>>>

    suspend fun getTimeDataFromStore(): Flow<StoreResponse<TimeProgress>>

    suspend fun updateTask(taskUpdateInfo: TaskUpdateInfo): Flow<StoreResponse<UpdateDataResponse>>

    suspend fun addTask(addTaskInfo: AddTaskInfo): Flow<StoreResponse<UpdateDataResponse>>

    suspend fun refreshTaskDataFromStore()

    suspend fun flushCacheFromStore()

    fun areTasksFromPreviousDay() : Boolean

    suspend fun getTaskById(taskId: String): Task

    data class TaskUpdateInfo(
        val id: String,
        val name: String,
        val subtaskId: String?,
        val service: String,
        val expectedDurationSeconds: Long,
        val actualDurationSeconds: Long?,
        val updateType: TaskUpdateType
    )

}
