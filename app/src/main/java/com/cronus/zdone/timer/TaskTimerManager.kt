package com.cronus.zdone.timer

import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.cronus.zdone.home.TasksScreen
import com.cronus.zdone.service.TaskTimerForegroundService
import com.cronus.zdone.timer.TimerState.*
import io.reactivex.BackpressureStrategy
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.reactive.asFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskTimerManager @Inject constructor(private val context: Context, private val taskTimerFactory: TaskTimerFactory) {

    val workManager = WorkManager.getInstance(context)
    internal var taskTimerWorker: OneTimeWorkRequest? = null
    var timer: Flow<Long> = emptyFlow()
    var _taskTimerState = BehaviorSubject.createDefault(Stopped())
    var taskTimerState: Flow<TimerState> = _taskTimerState.hide().toFlowable(BackpressureStrategy.BUFFER).asFlow()

    fun cancelTimer() {
        taskTimerWorker?.let {
            workManager.cancelWorkById(it.id)
            taskTimerWorker = null
        }
        context.stopService(Intent(context, TaskTimerForegroundService::class.java))
    }

    fun startTimer(task: TasksScreen.DisplayedTask) {
        CoroutineScope(Dispatchers.Main).launch {
            timer = taskTimerFactory.ofFlow(task.lengthMins)
        }
        val intent = Intent(context, TaskTimerForegroundService::class.java)
//        intent.addTask(task)
        context.startService(intent)
        val taskTimerWorkData = workDataOf(
                TaskTimerWorker.TASK_NAME_KEY to task.name
        )
        taskTimerWorker = OneTimeWorkRequestBuilder<TaskTimerWorker>()
                .setInputData(taskTimerWorkData)
                .setInitialDelay(task.lengthMins.toLong(), TimeUnit.MINUTES)
                .build()
        workManager.enqueue(taskTimerWorker!!)
    }

}
