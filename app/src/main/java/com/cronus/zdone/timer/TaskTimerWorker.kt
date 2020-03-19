package com.cronus.zdone.timer

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.cronus.zdone.MainActivity
import com.cronus.zdone.R
import com.cronus.zdone.ZdoneApplication

class TaskTimerWorker(appContext: Context, workerParams: WorkerParameters) :
        Worker(appContext, workerParams) {

    private val NOTIFICATION_ID = 1234321

    companion object {
        val TASK_NAME_KEY = "TASK_NAME"
    }

    override fun doWork(): Result {
        val taskName = inputData.getString(TASK_NAME_KEY)!!
        val displayTaskName =
                if (taskName.length > 7) taskName.substring(0, 7) + "..." else taskName
        val builder =
            NotificationCompat.Builder(applicationContext, ZdoneApplication.CHANNEL_ID)
                .setSmallIcon(R.drawable.start_task)
                .setContentTitle("Did you finish \"$displayTaskName\"?")
                .setContentText("This task was supposed to be done by now.")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("This task was supposed to be done by now. Remember to check it off when finished. Now get back to work!")
                )
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setContentIntent(
                    PendingIntent.getActivity(
                        applicationContext,
                        0,
                        MainActivity.getLaunchIntent(applicationContext),
                        0
                    )
                )

        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, builder.build())
        return Result.success()
    }
}