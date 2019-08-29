package com.cronus.zdone.dagger

import com.cronus.zdone.api.ApiTokenManager
import dagger.Component
import javax.inject.Singleton

@Component(modules = arrayOf(
        AndroidModule::class,
        AppModule::class))
@Singleton
interface AppComponent {

    fun apiTokenManager(): ApiTokenManager

    fun screenComponentBuilder(): ScreenComponent.Builder
}