package com.cronus.zdone.dagger

import com.cronus.zdone.home.HomeScreen
import com.cronus.zdone.login.LoginScreen
import com.cronus.zdone.settings.SettingsScreen
import dagger.Subcomponent

@Subcomponent(modules = arrayOf(
        ScreenImplModule::class
))
interface ScreenComponent {

    fun homeScreen(): HomeScreen

    fun loginScreen(): LoginScreen

    fun settingsScreen(): SettingsScreen

    @Subcomponent.Builder
    interface Builder {
        fun screenModule(module: ScreenImplModule): Builder

        fun build(): ScreenComponent
    }
}