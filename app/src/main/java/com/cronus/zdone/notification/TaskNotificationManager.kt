package com.cronus.zdone.notification

import androidx.annotation.VisibleForTesting
import com.cronus.zdone.AppDispatchers
import com.cronus.zdone.timer.TaskExecutionManager
import com.cronus.zdone.timer.TaskExecutionState
import com.cronus.zdone.util.Do
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

// Shows ongoing notification for task in progress, with ability to complete/defer directly from the notification
@Singleton
class TaskNotificationManager @VisibleForTesting internal constructor(private val notificationShower: TaskNotificationShower) {

    companion object {

        fun from(notificationShower: TaskNotificationShower, taskExecutionManager: TaskExecutionManager): TaskNotificationManager {
            val notificationManager = TaskNotificationManager(notificationShower)
            notificationManager.listenForEvents(taskExecutionManager)
            return notificationManager
        }

    }

    fun listenForEvents(taskExecutionManager: TaskExecutionManager) {
        CoroutineScope(Dispatchers.Main).launch {
            taskExecutionManager.currentTaskExecutionData
                .collect {
                    handleTaskExecutionState(it)
                }
        }
    }

    @VisibleForTesting
    internal fun handleTaskExecutionState(it: TaskExecutionState) {
        Do exhaustive when (it) {
            TaskExecutionState.WaitingForTasks -> notificationShower.hideNotification()
            is TaskExecutionState.TaskRunning -> notificationShower.showNotification()
            TaskExecutionState.AllTasksCompleted -> notificationShower.hideNotification()
        }
    }

}
