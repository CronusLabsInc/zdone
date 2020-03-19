package com.cronus.zdone

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.cronus.zdone.dagger.AndroidModule
import com.cronus.zdone.dagger.AppComponent
import com.cronus.zdone.dagger.DaggerAppComponent
import net.danlew.android.joda.JodaTimeAndroid

class ZdoneApplication : Application() {

    lateinit var component: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        component = DaggerAppComponent.builder()
                .androidModule(AndroidModule(this))
                .build()
        createNotificationChannel()
        JodaTimeAndroid.init(this)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notif_channel_name)
            val descriptionText = getString(R.string.notif_channel_description)
            var importance = NotificationManager.IMPORTANCE_DEFAULT
            var channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // make injector available everywhere using static instance
    companion object {
        val CHANNEL_ID = "ZDONE_CHANNEL_ID"
        private var INSTANCE: ZdoneApplication? = null

        @JvmStatic
        fun get(): ZdoneApplication = INSTANCE!! // safe as instance is set in onCreate()
    }
}