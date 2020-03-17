package com.cronus.zdone.api

import com.cronus.zdone.api.model.Task
import com.cronus.zdone.api.model.TimeProgress
import com.cronus.zdone.api.model.UpdateDataResponse
import com.cronus.zdone.home.TasksScreen
import com.dropbox.android.external.store4.StoreResponse
import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow

interface TasksRepository {

    fun getTasks(): Observable<List<Task>>

    suspend fun getTasksFromStore(): Flow<StoreResponse<List<Task>>>

    fun getTimeData(): Observable<TimeProgress>

    suspend fun getTimeDataFromStore(): Flow<StoreResponse<TimeProgress>>

    fun taskCompleted(taskUpdateInfo: TaskUpdateInfo): Observable<UpdateDataResponse>

    fun deferTask(taskUpdateInfo: TaskUpdateInfo): Observable<UpdateDataResponse>

    fun refreshTaskData()

    fun flushCache()

    fun updateWorkTime(maxWorkMins: Int)

    fun taskIsPreviousDay(task: TasksScreen.DisplayedTask): Boolean

    data class TaskUpdateInfo(
        val id: String,
        val subtaskId: String?,
        val service: String,
        val duration_seconds: Long?
    )

}
