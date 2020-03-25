package com.cronus.zdone.settings

import com.cronus.zdone.WorkTimeManager
import com.cronus.zdone.api.ApiTokenManager
import com.cronus.zdone.api.TasksRepository
import com.cronus.zdone.api.model.UpdateDataResponse
import com.dropbox.android.external.store4.ResponseOrigin
import com.dropbox.android.external.store4.StoreResponse
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class SettingsScreenTest {

    private val testDispatcher = TestCoroutineDispatcher()

    val apiTokenManager = mockk<ApiTokenManager>()
    val workTimeManager = mockk<WorkTimeManager>(relaxed = false)
    val tasksRepository = mockk<TasksRepository>()
    val view = mockk<SettingsView>(relaxed = true)
    val settingsScreen = SettingsScreen(apiTokenManager, workTimeManager, tasksRepository, mockk(), mockk(), mockk())

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { workTimeManager.setMaxWorkMins(any()) } returns flow { StoreResponse.Data(UpdateDataResponse("success"), ResponseOrigin.Fetcher) }
        settingsScreen.view = view
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun udpateWorkTime_emptyString() {
        settingsScreen.udpateWorkTime("")

        verify { view.setWorkTime(0) }
        coVerify { workTimeManager.setMaxWorkMins(0) }
    }

    @Test
    fun updateWorkTime_tooBigTime() {
        settingsScreen.udpateWorkTime("14401")

        verify { view.setWorkTime(1440) }
        coVerify { workTimeManager.setMaxWorkMins(1440) }
    }

    @Test
    fun updateWorkTime_negativeTime() {
        settingsScreen.udpateWorkTime("-1")

        verify { view.setWorkTime(0) }
        coVerify { workTimeManager.setMaxWorkMins(0) }
    }

    @Test
    fun updateWorkTime_nonNumberInput() {
        settingsScreen.udpateWorkTime("abcd")

        verify { view.setWorkTime(0) }
        coVerify { workTimeManager.setMaxWorkMins(0) }
    }

    @Test
    fun updateWorkTime_validInput() {
        settingsScreen.udpateWorkTime("250")

        verify { view.setWorkTime(250) }
        coVerify { workTimeManager.setMaxWorkMins(250) }
    }


}