package com.cronus.zdone.timer

import com.cronus.zdone.api.model.Task
import io.reactivex.BackpressureStrategy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.reactive.asFlow

class FakeTaskExecutionManager : TaskExecutionManager {

    private val _currentTaskExecutionData = BehaviorSubject.create<TaskExecutionState>()
    override val currentTaskExecutionData: Flow<TaskExecutionState> =
        _currentTaskExecutionData
            .hide()
            .toFlowable(BackpressureStrategy.ERROR)
            .asFlow()

    fun emitExecutionState(state: TaskExecutionState) {
        _currentTaskExecutionData.onNext(state)
    }

    override fun cancelTasks() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun startTasks(tasksToDo: List<Task>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun startNextTask() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}