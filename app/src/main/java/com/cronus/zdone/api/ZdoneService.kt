package com.cronus.zdone.api

import com.cronus.zdone.api.model.AddTaskInfo
import com.cronus.zdone.api.model.Task
import com.cronus.zdone.api.model.TaskStatusUpdate
import com.cronus.zdone.api.model.Tasks
import com.cronus.zdone.api.model.UpdateDataResponse
import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ZdoneService {

    @GET("/api")
    fun getTaskInfo(): Observable<Tasks>

    @GET("/api")
    suspend fun getTaskInfoFlow(): Tasks

    @POST("/api/update_task")
    fun updateTask(@Body taskStatus: TaskStatusUpdate): Observable<UpdateDataResponse>

    @POST("/api/update_task")
    suspend fun updateTaskAsync(@Body taskStatus: TaskStatusUpdate): UpdateDataResponse

    @POST("/api/update_time")
    fun updateWorkTime(@Body body: Map<String, Int>): Observable<UpdateDataResponse>

    @POST("/api/update_time")
    suspend fun updateWorkTimeAsync(@Body body: Map<String, Int>): UpdateDataResponse

    @POST("/api/add_task")
    suspend fun addTaskAsync(@Body newTaskInfo: AddTaskInfo): UpdateDataResponse

}