package com.cronus.zdone.dagger

import com.cronus.zdone.home.HomeScreen
import com.cronus.zdone.login.LoginScreen
import com.cronus.zdone.settings.SettingsScreen
import com.cronus.zdone.stats.DailyStatsScreen
import com.cronus.zdone.stats.log.DailyStatsLogScreen
import dagger.Subcomponent

@Subcomponent(modules = arrayOf(
        ScreenImplModule::class
))
interface ScreenComponent {

    fun homeScreen(): HomeScreen

    fun loginScreen(): LoginScreen

    fun settingsScreen(): SettingsScreen

    fun dailyStatsScreen(): DailyStatsScreen

    @Subcomponent.Builder
    interface Builder {
        fun screenModule(module: ScreenImplModule): Builder

        fun build(): ScreenComponent
    }
}