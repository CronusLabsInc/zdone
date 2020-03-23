package com.cronus.zdone.timer

import android.util.Log
import com.cronus.zdone.api.TasksRepository
import com.cronus.zdone.api.model.Task
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import javax.inject.Inject
import javax.inject.Singleton

interface TaskExecutionManager {

    val currentTaskExecutionData: Flow<TaskExecutionState>

    fun cancelTasks()

    suspend fun startTasks(tasksToDo: List<Task>)

    suspend fun startNextTask()

}

@Singleton
class RealTaskExecutionManager @Inject constructor() : TaskExecutionManager {

    private var tasks = mutableListOf<Task>()
    // used to unsubscribe from timer for a task when the task is completed/deferred/stopped
    private var currentTaskJob: Job? = null
    private val _currentTaskExecutionData = BehaviorSubject.createDefault<TaskExecutionState>(
        TaskExecutionState.WaitingForTasks)
    override val currentTaskExecutionData: Flow<TaskExecutionState> =
        _currentTaskExecutionData
            .toFlowable(BackpressureStrategy.BUFFER)
            .map {
                Log.d("ZDONE_TASK_EXECUTION", it.toString())
                it }
            .asFlow()

    override fun cancelTasks() {
        tasks = mutableListOf()
        currentTaskJob?.cancel()
        _currentTaskExecutionData.onNext(TaskExecutionState.WaitingForTasks)
    }

    override suspend fun startTasks(tasksToDo: List<Task>) = coroutineScope {
        tasks = tasksToDo.toMutableList()
        startNextTask()
    }

    override suspend fun startNextTask() {
        currentTaskJob?.cancel()
        if (tasks.isEmpty()) {
            emitTasksCompleted()
            return
        }
        val nextTask = tasks.removeAt(0)
        startTask(nextTask)
    }

    private fun emitTasksCompleted() {
        _currentTaskExecutionData.onNext(TaskExecutionState.AllTasksCompleted)
    }

    private suspend fun startTask(task: Task) {
        _currentTaskExecutionData.onNext(
            TaskExecutionState.TaskRunning(task, task.lengthMins * 60L)
        )
        currentTaskJob = CoroutineScope(Dispatchers.Default).launch {
            TaskTimerFactory().ofFlow(task.lengthMins)
                .map { timeRemaining ->
                    _currentTaskExecutionData.onNext(
                        TaskExecutionState.TaskRunning(task, timeRemaining)
                    )
                }
                .collect()
        }
    }
}

sealed class TaskExecutionState {
    object WaitingForTasks : TaskExecutionState()
    data class TaskRunning(val task: Task, val secsRemaining: Long) : TaskExecutionState()
    object AllTasksCompleted : TaskExecutionState()
}