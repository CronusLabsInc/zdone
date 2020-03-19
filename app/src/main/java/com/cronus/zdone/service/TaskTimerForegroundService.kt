package com.cronus.zdone.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.cronus.zdone.MainActivity
import com.cronus.zdone.R
import com.cronus.zdone.ZdoneApplication
import com.cronus.zdone.api.model.Task
import com.cronus.zdone.timer.TaskExecutionManager
import com.cronus.zdone.timer.TaskExecutionState
import com.cronus.zdone.util.Do
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class TaskTimerForegroundService : Service() {

    private val NOTIFICATION_ID = 142579

    @Inject
    lateinit var taskExecutionManager: TaskExecutionManager
    private var timerJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val result =  super.onStartCommand(intent, flags, startId)
        (application as ZdoneApplication).component.inject(this)
        if (timerJob == null) {
            timerJob = CoroutineScope(Dispatchers.IO).launch {
                taskExecutionManager.currentTaskExecutionData.collect { taskState ->
                    Do exhaustive when(taskState) {
                        TaskExecutionState.WaitingForTasks -> stopSelf()
                        is TaskExecutionState.TaskRunning -> updateNotification(taskState.task, taskState.secsRemaining)
                        TaskExecutionState.AllTasksCompleted -> stopSelf()
                    }
                }
            }
        }

        return result
    }

    private fun updateNotification(task: Task, timeRemainingSecs: Long) {
        val completedIntent = Intent(applicationContext, UpdateTaskService::class.java)
        completedIntent.putExtra(UpdateTaskService.UPDATE_TYPE_INTENT_KEY, UpdateTaskService.RequestCodes.COMPLETED.name)
        val deferIntent = Intent(applicationContext, UpdateTaskService::class.java)
        deferIntent.putExtra(UpdateTaskService.UPDATE_TYPE_INTENT_KEY, UpdateTaskService.RequestCodes.DEFERRED.name)

        val builder =
            NotificationCompat.Builder(applicationContext, ZdoneApplication.CHANNEL_ID)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.ic_zdone_notification_logo_2)
                .setContentTitle(getTitleForRemainingTime(task.name, timeRemainingSecs))
                .setContentText(formattedTimeString(timeRemainingSecs))
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .addAction(0, "Completed", PendingIntent.getService(
                    applicationContext,
                    12345123,
                    completedIntent,
                    0))
                .addAction(0, "Defer", PendingIntent.getService(
                    applicationContext,
                    54322,
                    deferIntent,
                    0))
                .setContentIntent(
                    PendingIntent.getActivity(
                        applicationContext,
                        0,
                        MainActivity.getLaunchIntent(applicationContext),
                        0
                    )
                )
        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, builder.build())
    }

    private fun getTitleForRemainingTime(taskName: String, timeRemainingSecs: Long): CharSequence? =
        if (timeRemainingSecs > 0) "Time remaining for: $taskName" else "Time's up for: $taskName!"

    private fun formattedTimeString(timeRemainingSecs: Long): String {
        return if (timeRemainingSecs > 0) "${timeRemainingSecs / 60}:${String.format("%02d", timeRemainingSecs % 60)}" else "Time's Up!"
    }

    override fun onDestroy() {
        NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_ID)
        timerJob?.cancel()
        super.onDestroy()
    }

}