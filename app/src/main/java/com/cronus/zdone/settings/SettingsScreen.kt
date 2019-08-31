package com.cronus.zdone.settings

import android.content.Context
import com.cronus.zdone.R
import com.cronus.zdone.WorkTimeManager
import com.cronus.zdone.api.ApiTokenManager
import com.cronus.zdone.api.TasksRepository
import com.cronus.zdone.dagger.ScreenInjector
import com.wealthfront.magellan.NavigationType
import com.wealthfront.magellan.Screen
import javax.inject.Inject

class SettingsScreen @Inject constructor(
        val apiTokenManager: ApiTokenManager,
        val workTimeManager: WorkTimeManager,
        val tasksRepository: TasksRepository) : Screen<SettingsView>() {

    override fun createView(context: Context) = SettingsView(context)

    override fun onShow(context: Context?) {
        view?.setApiKey(apiTokenManager.getToken())
        view?.setWorkTime(workTimeManager.getDefaultWorkTime())
    }

    fun updateApiKey(newApiKey: String) {
        apiTokenManager.putToken(newApiKey)
        tasksRepository.flushCache()
    }

    fun udpateWorkTime(newWorkTime: String) {
        newWorkTime.toIntOrNull()?.let { newWorkInt ->
            when {
                newWorkInt < 0 -> view?.setWorkTime(0)
                newWorkInt > 1440 -> view?.setWorkTime(1440)
                else -> workTimeManager.setDefaultWorkTime(newWorkInt)
            }
        } ?: view?.setWorkTime(0)
    }

    override fun getTitle(context: Context): String {
        return context.getString(R.string.settings)
    }

    fun logout() {
        apiTokenManager.putToken("")
        tasksRepository.flushCache()
        navigator.navigate({
            it.clear()
            it.push(ScreenInjector.get().loginScreen())
        }, NavigationType.SHOW)
    }
}
