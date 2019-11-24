package com.cronus.zdone.home

import android.content.SharedPreferences
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskShowerStrategyProvider @Inject constructor(sharedPreferences: SharedPreferences) {

    private var currentStrategy = AtomicReference<TaskShowingStrategy>()

    init {
        if (sharedPreferences.getBoolean("section_tasks_mode_key", false)) {
            setStrategy(TimeOfDayTaskShowingStrategy())
        } else {
            setStrategy(NoFilterTaskShowingStrategy())
        }
    }

    fun getStrategy() = currentStrategy.get()

    fun setStrategy(newStrategy: TaskShowingStrategy) = currentStrategy.set(newStrategy)
}
