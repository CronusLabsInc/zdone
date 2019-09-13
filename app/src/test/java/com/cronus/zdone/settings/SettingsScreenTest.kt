package com.cronus.zdone.settings

import com.cronus.zdone.WorkTimeManager
import com.cronus.zdone.api.ApiTokenManager
import com.cronus.zdone.api.TasksRepository
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class SettingsScreenTest {

    val apiTokenManager = mockk<ApiTokenManager>()
    val workTimeManager = mockk<WorkTimeManager>(relaxed = true)
    val tasksRepository = mockk<TasksRepository>()
    val view = mockk<SettingsView>(relaxed = true)
    val settingsScreen = SettingsScreen(apiTokenManager, workTimeManager, tasksRepository)

    @Before
    fun setUp() {
        settingsScreen.view = view
    }

    @Test
    fun udpateWorkTime_emptyString() {
        settingsScreen.udpateWorkTime("")

        verify { view.setWorkTime(0) }
        verify { workTimeManager.setMaxWorkMins(0) }
    }

    @Test
    fun updateWorkTime_tooBigTime() {
        settingsScreen.udpateWorkTime("14401")

        verify { view.setWorkTime(1440) }
        verify { workTimeManager.setMaxWorkMins(1440) }
    }

    @Test
    fun updateWorkTime_negativeTime() {
        settingsScreen.udpateWorkTime("-1")

        verify { view.setWorkTime(0) }
        verify { workTimeManager.setMaxWorkMins(0) }
    }

    @Test
    fun updateWorkTime_nonNumberInput() {
        settingsScreen.udpateWorkTime("abcd")

        verify { view.setWorkTime(0) }
        verify { workTimeManager.setMaxWorkMins(0) }
    }

    @Test
    fun updateWorkTime_validInput() {
        settingsScreen.udpateWorkTime("250")

        verify { view.setWorkTime(250) }
        verify { workTimeManager.setMaxWorkMins(250) }
    }


}