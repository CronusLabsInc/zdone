package com.cronus.zdone.dagger

import com.cronus.zdone.api.ApiTokenManager
import com.cronus.zdone.timer.TaskTimerForegroundService
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
}