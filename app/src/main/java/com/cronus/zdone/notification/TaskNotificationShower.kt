package com.cronus.zdone.notification

import android.content.Context
import android.content.Intent
import com.cronus.zdone.service.TaskTimerForegroundService
import javax.inject.Inject
import javax.inject.Singleton

interface TaskNotificationShower {

    fun showNotification()

    fun hideNotification()

}

@Singleton
class OngoingNotificationShower @Inject constructor(private val context: Context) : TaskNotificationShower {

    override fun showNotification() {
        val intent = Intent(context, TaskTimerForegroundService::class.java)
        context.startService(intent)
    }

    override fun hideNotification() {
        context.stopService(Intent(context, TaskTimerForegroundService::class.java))
    }

}