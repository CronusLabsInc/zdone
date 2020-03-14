package com.cronus.zdone

import com.cronus.zdone.api.TasksRepository
import com.cronus.zdone.api.model.Task
import com.cronus.zdone.api.model.TimeProgress
import com.cronus.zdone.api.model.UpdateDataResponse
import com.cronus.zdone.home.TasksScreen
import io.reactivex.Observable

class TestTasksRepo : TasksRepository {

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
        return Observable.just(TimeProgress(15, 85, 9999))
    }

    override fun getTasks(): Observable<List<Task>> {
        return Observable.just(
            listOf(
                Task("abcd", "Anki", null, "habitica", 15)))
    }

    override fun refreshTaskData() {}

    override fun flushCache() {
        // do nothing
    }

}