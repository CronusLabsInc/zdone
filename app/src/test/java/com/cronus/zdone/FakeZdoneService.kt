package com.cronus.zdone

import com.cronus.zdone.api.ZdoneService
import com.cronus.zdone.api.model.*
import io.reactivex.Observable

class FakeZdoneService : ZdoneService {

    val tasks = Tasks(
        listOf(Task("abcd", "Anki", null, "toodledo", 15)),
        TimeProgress(15, 85, 9999)
    )

    override suspend fun getTaskInfoFlow(): Tasks {
        return tasks
    }

    override fun updateTask(taskStatus: TaskStatusUpdate): Observable<UpdateDataResponse> {
        return Observable.just(UpdateDataResponse("success"))
    }

    override fun getTaskInfo(): Observable<Tasks> {
        return Observable.just(tasks)
    }

    override fun updateWorkTime(body: Map<String, Int>): Observable<UpdateDataResponse> {
        return Observable.just(UpdateDataResponse("success"))
    }

    override suspend fun updateTaskAsync(taskStatus: TaskStatusUpdate): UpdateDataResponse {
        return UpdateDataResponse("success")
    }

    override suspend fun updateWorkTimeAsync(body: Map<String, Int>): UpdateDataResponse {
        return UpdateDataResponse("success")
    }
}