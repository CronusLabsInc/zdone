package com.cronus.zdone.api

import com.cronus.zdone.api.model.Task
import com.cronus.zdone.api.model.TimeProgress
import com.cronus.zdone.api.model.UpdateDataResponse
import com.cronus.zdone.home.TasksScreen
import io.reactivex.Observable

interface TasksRepository {

    fun getTasks(): Observable<List<Task>>

    fun taskCompleted(task: TasksScreen.DisplayedTask): Observable<UpdateDataResponse>

    fun deferTask(task: TasksScreen.DisplayedTask): Observable<UpdateDataResponse>

    fun getTimeData(): Observable<TimeProgress>

    fun refreshTaskData()

    fun flushCache()

    fun updateWorkTime(maxWorkMins: Int)

    fun taskIsPreviousDay(task: TasksScreen.DisplayedTask): Boolean

}
