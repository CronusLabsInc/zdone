package com.cronus.zdone.dagger

import android.content.Context
import com.cronus.zdone.BuildConfig
import com.cronus.zdone.NoOpToaster
import com.cronus.zdone.RealToaster
import com.cronus.zdone.Toaster
import com.cronus.zdone.notification.OngoingNotificationShower
import com.cronus.zdone.notification.TaskNotificationShower
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [AndroidModule.Bindings::class])
class AndroidModule(val context: Context) {

    @Provides
    fun context() = context

    @Provides
    fun sharedPreferences(context: Context) = context.getSharedPreferences("zdone", Context.MODE_PRIVATE)

    @Provides
    fun toaster(context: Context): Toaster {
        return if (BuildConfig.DEBUG) RealToaster(context) else NoOpToaster()
    }

    @Module
    interface Bindings {

        @Binds
        fun taskNotificationShower(ongoingNotificationShower: OngoingNotificationShower): TaskNotificationShower

    }

}