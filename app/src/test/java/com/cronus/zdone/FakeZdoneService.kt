package com.cronus.zdone

import com.cronus.zdone.api.ZdoneService
import com.cronus.zdone.api.model.*
import io.reactivex.Observable

class FakeZdoneService : ZdoneService {

    override fun updateTask(taskStatus: TaskStatusUpdate): Observable<UpdateDataResponse> {
        return Observable.just(UpdateDataResponse("success"))
    }

    override fun getTaskInfo(): Observable<Tasks> {
        return Observable.just(
            Tasks(
                listOf(Task("abcd", "Anki", null, "toodledo", 15)),
                TimeProgress(15, 85, 9999)
            )
        )
    }

    override fun updateWorkTime(body: Map<String, Int>): Observable<UpdateDataResponse> {
        return Observable.just(UpdateDataResponse("success"))
    }
}