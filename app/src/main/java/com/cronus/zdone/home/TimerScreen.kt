package com.cronus.zdone.home

import android.content.Context
import com.cronus.zdone.R
import com.cronus.zdone.api.TasksRepository
import com.wealthfront.magellan.Screen
import javax.inject.Inject

class TimerScreen @Inject constructor(tasksRepository: TasksRepository) : Screen<TimerView>() {

    override fun createView(context: Context): TimerView = TimerView(context)

    override fun getTitle(context: Context): String {
        return context.getString(R.string.timer)
    }

}