package com.cronus.zdone.api

import com.cronus.zdone.api.model.Task
import com.cronus.zdone.api.model.TimeProgress
import com.cronus.zdone.api.model.UpdateTaskResponse
import com.cronus.zdone.home.HomeScreen
import io.reactivex.Observable

interface TasksRepository {

    fun getTasks(): Observable<List<Task>>

    fun taskCompleted(task: HomeScreen.DisplayedTask): Observable<UpdateTaskResponse>

    fun deferTask(task: HomeScreen.DisplayedTask): Observable<UpdateTaskResponse>

    fun getTimeData(): Observable<TimeProgress>

    fun refreshTaskData(): Observable<List<Task>>

    fun flushCache()

}
