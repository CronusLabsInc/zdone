package com.cronus.zdone.dagger

import android.content.Context
import android.os.Vibrator
import androidx.room.Room
import androidx.room.migration.Migration
import com.cronus.zdone.BuildConfig
import com.cronus.zdone.NoOpToaster
import com.cronus.zdone.RealToaster
import com.cronus.zdone.RealUserPreferences
import com.cronus.zdone.Toaster
import com.cronus.zdone.UserPreferences
import com.cronus.zdone.notification.OngoingNotificationShower
import com.cronus.zdone.notification.RealTaskFinishedBuzzer
import com.cronus.zdone.notification.TaskFinishedBuzzer
import com.cronus.zdone.notification.TaskNotificationShower
import com.cronus.zdone.stats.TaskEventsDatabase
import com.cronus.zdone.stats.fake.FakeTaskEventsDao
import com.cronus.zdone.stats.migrations.Migration1to2
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [AndroidModule.Bindings::class])
class AndroidModule(val context: Context) {

    @Provides
    fun context() = context

    @Provides
    fun sharedPreferences(context: Context) =
        context.getSharedPreferences("zdone", Context.MODE_PRIVATE)

    @Provides
    fun toaster(context: Context): Toaster {
        return if (BuildConfig.DEBUG) RealToaster(context) else NoOpToaster()
    }

    @Provides
    @Singleton
    fun taskEventsDatabase(context: Context): TaskEventsDatabase {
        return Room.databaseBuilder(
            context,
            TaskEventsDatabase::class.java,
            "zdone"
        )
            .addMigrations(Migration1to2)
            .build()
    }

    @Provides
    fun taskEventsDao(taskEventsDatabase: TaskEventsDatabase) =
        taskEventsDatabase.taskEventsDao()
//        if (BuildConfig.DEBUG) FakeTaskEventsDao() else taskEventsDatabase.taskEventsDao()

    @Provides
    fun vibrator(context: Context): Vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    @Module
    interface Bindings {

        @Binds
        fun taskNotificationShower(ongoingNotificationShower: OngoingNotificationShower): TaskNotificationShower

        @Binds
        fun userPreferences(realUserPreferences: RealUserPreferences): UserPreferences

        @Binds
        @Singleton
        fun taskFinishedBuzzer(realTaskFinishedBuzzer: RealTaskFinishedBuzzer): TaskFinishedBuzzer

    }

}