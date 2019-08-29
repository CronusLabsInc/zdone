package com.cronus.zdone

import com.cronus.zdone.api.ZdoneService
import com.cronus.zdone.api.model.*
import io.reactivex.Observable
import retrofit2.http.Query

class TestZdoneServiceImpl : ZdoneService {

    override fun updateTask(taskStatus: TaskStatusUpdate): Observable<UpdateTaskResponse> {
        return Observable.just(UpdateTaskResponse("success"))
    }

    override fun getTaskInfo(@Query(value = "time") workTimeLimit: Int): Observable<Tasks> {
        return Observable.just(
                Tasks(
                        listOf(Task("abcd", "Anki", null, "toodledo", 15)),
                        TimeProgress(15, 85)))
    }
}