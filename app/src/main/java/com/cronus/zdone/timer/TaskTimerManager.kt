package com.cronus.zdone.timer

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.cronus.zdone.home.TasksScreen
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskTimerManager @Inject constructor(context: Context) {

    val workManager = WorkManager.getInstance(context)
    internal var taskTimerWorker: OneTimeWorkRequest? = null

    fun cancelTimer() {
        taskTimerWorker?.let {
            workManager.cancelWorkById(it.id)
            taskTimerWorker = null
        }
    }

    fun startTimer(task: TasksScreen.DisplayedTask) {
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