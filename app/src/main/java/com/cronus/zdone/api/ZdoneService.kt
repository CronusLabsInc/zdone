package com.cronus.zdone.api

import com.cronus.zdone.api.model.TaskStatusUpdate
import com.cronus.zdone.api.model.Tasks
import com.cronus.zdone.api.model.UpdateTaskResponse
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ZdoneService {

    @GET("/api")
    fun getTaskInfo(@Query("time") workTimeLimit: Int): Observable<Tasks>

    @POST("/api/update_task")
    fun updateTask(@Body taskStatus: TaskStatusUpdate): Observable<UpdateTaskResponse>

}