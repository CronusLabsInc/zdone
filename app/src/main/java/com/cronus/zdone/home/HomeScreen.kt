package com.cronus.zdone.home

import android.content.Context
import android.view.Menu
import com.cronus.zdone.R
import com.wealthfront.magellan.Screen
import com.wealthfront.magellan.ScreenGroup
import javax.inject.Inject

class HomeScreen @Inject constructor(timerScreen: TimerScreen, tasksScreen: TasksScreen) :
    ScreenGroup<Screen<*>, HomeView>(listOf(timerScreen, tasksScreen)) {

    override fun createView(context: Context): HomeView = HomeView(context, screens)

    override fun onShow(context: Context?) {
        super.onShow(context)
        screens.map {
            view.addTabView(it.getView())
        }
    }

    override fun getTitle(context: Context): String {
        return context.getString(R.string.app_name)
    }

    override fun onUpdateMenu(menu: Menu) {
        menu.findItem(R.id.settings).setVisible(true)
    }


}