package com.cronus.zdone.settings

import android.content.Context
import com.cronus.zdone.R
import com.cronus.zdone.WorkTimeManager
import com.cronus.zdone.api.ApiTokenManager
import com.cronus.zdone.api.TasksRepository
import com.cronus.zdone.dagger.ScreenInjector
import com.wealthfront.magellan.NavigationType
import com.wealthfront.magellan.rx2.RxScreen
import javax.inject.Inject

class SettingsScreen @Inject constructor(
    val apiTokenManager: ApiTokenManager,
    val workTimeManager: WorkTimeManager,
    val tasksRepository: TasksRepository
) : RxScreen<SettingsView>() {

    override fun createView(context: Context) = SettingsView(context)

    override fun onSubscribe(context: Context) {
        view?.setApiKey(apiTokenManager.getToken())
        view?.showLoadingWorkTime()
        autoDispose(workTimeManager.currentWorkTime.subscribe {
            view?.setWorkTime(it)
        })
        view?.setLargeFingersMode(isLargeFingersModeEnabled(context))
    }

    private fun isLargeFingersModeEnabled(context: Context): Boolean {
        return context.getSharedPreferences("zdone", Context.MODE_PRIVATE)
            .getBoolean("large_fingers_mode_key", false)
    }

    fun updateApiKey(newApiKey: String) {
        apiTokenManager.putToken(newApiKey)
        tasksRepository.flushCache()
    }

    fun udpateWorkTime(newWorkTime: String) {
        val finalWorkTime = when {
            newWorkTime.toIntOrNull() == null -> 0
            newWorkTime.toInt() < 0 -> 0
            newWorkTime.toInt() > 1440 -> 1440
            else -> newWorkTime.toInt()
        }
        view?.setWorkTime(finalWorkTime)
        workTimeManager.setMaxWorkMins(finalWorkTime)
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

    fun setLargeFingersModeEnabled(enabled: Boolean) {
        activity?.let {
            it.getSharedPreferences("zdone", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("large_fingers_mode_key", enabled)
                .apply()
        }
    }
}
