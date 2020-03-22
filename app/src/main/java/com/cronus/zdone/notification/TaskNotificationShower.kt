package com.cronus.zdone.notification

import android.content.Context
import android.content.Intent
import android.os.Build
import android.system.Os
import com.cronus.zdone.service.TaskTimerForegroundService
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

interface TaskNotificationShower {

    fun showNotification()

    fun hideNotification()

}

@Singleton
class OngoingNotificationShower @Inject constructor(private val context: Context) :
    TaskNotificationShower {

    override fun showNotification() {
        val intent = Intent(context, TaskTimerForegroundService::class.java)
        context.startService(intent)
    }

    override fun hideNotification() {
        context.stopService(Intent(context, TaskTimerForegroundService::class.java))
    }

}