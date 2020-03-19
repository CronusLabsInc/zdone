package com.cronus.zdone.dagger

import com.cronus.zdone.api.ApiTokenManager
import com.cronus.zdone.notification.TaskNotificationManager
import com.cronus.zdone.service.TaskTimerForegroundService
import com.cronus.zdone.service.UpdateTaskService
import dagger.Component
import javax.inject.Singleton

@Component(modules = arrayOf(
        AndroidModule::class,
        AppModule::class))
@Singleton
interface AppComponent {

    fun apiTokenManager(): ApiTokenManager

    fun screenComponentBuilder(): ScreenComponent.Builder

    fun inject(taskTimerForegroundService: TaskTimerForegroundService)

    fun inject(updateTaskService: UpdateTaskService)

    fun taskNotificationManager(): TaskNotificationManager
}