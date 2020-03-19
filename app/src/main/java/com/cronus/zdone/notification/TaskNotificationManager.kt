package com.cronus.zdone.notification

import android.content.Context
import android.content.Intent
import com.cronus.zdone.service.TaskTimerForegroundService
import com.cronus.zdone.timer.TaskExecutionManager
import com.cronus.zdone.timer.TaskExecutionState
import com.cronus.zdone.util.Do
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

// Shows ongoing notification for task in progress, with ability to complete/defer directly from the notification
@Singleton
class TaskNotificationManager @Inject constructor(private val context: Context, taskExecutionManager: TaskExecutionManager) {

    init {
        CoroutineScope(Dispatchers.IO).launch {
            taskExecutionManager.currentTaskExecutionData
                .collect {
                    Do exhaustive when (it) {
                        TaskExecutionState.WaitingForTasks -> clearNotifications()
                        is TaskExecutionState.TaskRunning -> showRunningTaskNotification()
                        TaskExecutionState.AllTasksCompleted -> clearNotifications()
                    }
                }
        }
    }

    private fun showRunningTaskNotification() {
        val intent = Intent(context, TaskTimerForegroundService::class.java)
        context.startService(intent)
    }

    private fun clearNotifications() {
        context.stopService(Intent(context, TaskTimerForegroundService::class.java))
    }

}

