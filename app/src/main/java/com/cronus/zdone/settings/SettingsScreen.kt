package com.cronus.zdone.settings

import android.content.Context
import android.content.SharedPreferences
import com.cronus.zdone.R
import com.cronus.zdone.WorkTimeManager
import com.cronus.zdone.api.ApiTokenManager
import com.cronus.zdone.api.TasksRepository
import com.cronus.zdone.dagger.ScreenInjector
import com.cronus.zdone.home.NoFilterTaskShowingStrategy
import com.cronus.zdone.home.TaskShowerStrategyProvider
import com.cronus.zdone.home.TimeOfDayTaskShowingStrategy
import com.dropbox.android.external.store4.StoreResponse
import com.wealthfront.magellan.NavigationType
import com.wealthfront.magellan.rx2.RxScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsScreen @Inject constructor(
    val apiTokenManager: ApiTokenManager,
    val workTimeManager: WorkTimeManager,
    val tasksRepository: TasksRepository,
    val sharedPreferences: SharedPreferences,
    val taskShowerStrategyProvider: TaskShowerStrategyProvider
) : RxScreen<SettingsView>() {

    override fun createView(context: Context) = SettingsView(context)

    override fun onSubscribe(context: Context) {
        view?.setApiKey(apiTokenManager.getToken())
        view?.showLoadingWorkTime()
        autoDispose(workTimeManager.currentWorkTime.subscribe {
            view?.setWorkTime(it)
        })
        view?.setLargeFingersMode(isLargeFingersModeEnabled())
        view?.setSectionTasksByTimeOfDayMode(isSectionTasksByTimeOfDayEnabled())
    }

    private fun isSectionTasksByTimeOfDayEnabled(): Boolean {
        return sharedPreferences
            .getBoolean("section_tasks_mode_key", false)
    }

    private fun isLargeFingersModeEnabled(): Boolean {
        return sharedPreferences
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
        CoroutineScope(Dispatchers.Main).launch {
            launch { workTimeManager.setMaxWorkMins(finalWorkTime)
                .collect { response ->
                    when (response) {
                        is StoreResponse.Loading -> {}
                        is StoreResponse.Data -> tasksRepository.refreshTaskDataFromStore()
                        is StoreResponse.Error -> {}
                    }
                }}
        }
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
        sharedPreferences.edit()
            .putBoolean("large_fingers_mode_key", enabled)
            .apply()
    }

    fun setSectionTasksByTimeOfDayEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean("section_tasks_mode_key", enabled)
            .apply()
        taskShowerStrategyProvider.setStrategy(if (enabled) TimeOfDayTaskShowingStrategy() else NoFilterTaskShowingStrategy())
    }
}
