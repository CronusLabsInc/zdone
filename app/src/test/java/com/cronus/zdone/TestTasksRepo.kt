package com.cronus.zdone

import com.cronus.zdone.api.TasksRepository
import com.cronus.zdone.api.model.Task
import com.cronus.zdone.api.model.TimeProgress
import com.cronus.zdone.api.model.UpdateTaskResponse
import com.cronus.zdone.home.HomeScreen
import io.reactivex.Observable

class TestTasksRepo : TasksRepository {

    override fun deferTask(task: HomeScreen.DisplayedTask): Observable<UpdateTaskResponse> {
        return Observable.just(UpdateTaskResponse("success"))
    }

    override fun taskCompleted(task: HomeScreen.DisplayedTask): Observable<UpdateTaskResponse> {
        return Observable.just(UpdateTaskResponse("success"))
    }

    override fun getTimeData(): Observable<TimeProgress> {
        return Observable.just(TimeProgress(15, 85))
    }

    override fun getTasks(): Observable<List<Task>> {
        return Observable.just(listOf(
                Task("abcd", "Anki", null, "habitica", 15)
        )
        )
    }

    override fun refreshTaskData(): Observable<List<Task>> {
        return Observable.just(listOf(
                Task("abcd", "Anki", null, "habitica", 15)
        )
        )
    }

    override fun flushCache() {
        // do nothing
    }

}