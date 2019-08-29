package com.cronus.zdone

import com.cronus.zdone.api.TasksRepository
import com.cronus.zdone.api.model.UpdateTaskResponse
import com.cronus.zdone.home.HomeScreen
import com.cronus.zdone.home.HomeScreen.DisplayedTask
import com.cronus.zdone.home.HomeScreen.TaskProgressState.*
import com.cronus.zdone.home.HomeView
import com.cronus.zdone.timer.TaskTimerManager
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.spyk
import io.mockk.verify
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test

class HomeScreenTest {

    @RelaxedMockK
    lateinit var taskTimerManager: TaskTimerManager
    @RelaxedMockK
    lateinit var homeView: HomeView

    val testRepo = spyk<TasksRepository>(TestTasksRepo())

    lateinit var homeScreen: HomeScreen

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        homeScreen = HomeScreen(testRepo, taskTimerManager)
        homeScreen.view = homeView
    }

    @Test
    fun requestTaskData() {
        homeScreen.requestTaskData()
        val displayedTasks = homeScreen.getDisplayedTasks(testRepo.getTasks().blockingFirst())

        verify { homeView.setTasks(eq(displayedTasks)) }
        verify { homeView.setTimeProgress(85) }
    }

    @Test
    fun taskCompleted_noInProgressTask() {
        val task = DisplayedTask("fake-id", null, "Reading", "habitica", 30, false, true, READY)
        homeScreen.taskCompleted(task)

        verify { testRepo.taskCompleted(task) }
    }

    @Test
    fun taskCompleted_onFailure() {
        every { testRepo.taskCompleted(any()) } returns Observable.just(
                UpdateTaskResponse(
                        "failure",
                        "500 internal server error"
                )
        )
        val task = DisplayedTask("fake-id", null, "Reading", "habitica", 30, false, true, READY)
        homeScreen.taskCompleted(task)

        verify { testRepo.taskCompleted(task) }
        // check task data is refreshed
        verify { testRepo.getTasks() }
        verify { testRepo.getTimeData() }
    }

    @Test
    fun taskCompleted_withInProgressTask_completedOtherTask() {
        val task = DisplayedTask("fake-id", null, "Reading", "habitica", 30, false, true, WAITING)
        homeScreen.inProgressTask =
                DisplayedTask("other_id", null, "Writing", "habitica", 30, false, true, IN_PROGRESS)
        homeScreen.taskCompleted(task)

        verify { testRepo.taskCompleted(task) }
        assertThat(homeScreen.inProgressTask).isNotNull()
    }

    @Test
    fun taskCompleted_completedInProgressTask() {
        val task = DisplayedTask("fake-id", null, "Reading", "habitica", 30, false, true, WAITING)
        homeScreen.inProgressTask = task
        homeScreen.taskCompleted(task)

        verify { testRepo.taskCompleted(task) }
        assertThat(homeScreen.inProgressTask).isNull()
        verify { taskTimerManager.cancelTimer() }
        verify { homeView.setTasksProgressState(READY) }
    }

    @Test
    fun startTask() {
        val task = DisplayedTask("fake-id", null, "Reading", "habitica", 30, false, true, READY)
        homeScreen.startTask(task)

        assertThat(homeScreen.inProgressTask).isEqualTo(task)
        verify { taskTimerManager.startTimer(eq(task)) }
        verify { homeView.setInProgressTask(eq(task)) }
    }

    @Test
    fun pauseTask() {
        val task = DisplayedTask("fake-id", null, "Reading", "habitica", 30, false, true, READY)
        homeScreen.inProgressTask = task
        homeScreen.pauseTask(task)

        assertThat(homeScreen.inProgressTask).isNull()
        verify { taskTimerManager.cancelTimer() }
        verify { homeView.setTasksProgressState(READY) }
    }

    @Test
    fun deferTask_success() {
        val task = DisplayedTask("fake-id", null, "Reading", "habitica", 30, false, true, READY)
        homeScreen.deferTask(task)

        verify { testRepo.deferTask(task) }
    }

    @Test
    fun deferTask_failure() {
        val task = DisplayedTask("fake-id", null, "Reading", "habitica", 30, false, true, READY)
        every { testRepo.deferTask(any()) } returns Observable.just(
                UpdateTaskResponse(
                        "error",
                        "500 internal server error"
                )
        )
        homeScreen.deferTask(task)

        verify { homeView.setTasks(any()) } // should re-request task data on failure
    }

    @Test
    fun deferTask_apiError() {
        val task = DisplayedTask("fake-id", null, "Reading", "habitica", 30, false, true, READY)
        every { testRepo.deferTask(any()) } returns Observable.error(Exception("boom"))
        homeScreen.deferTask(task)

        verify { homeView.showError("boom") } // should re-request task data on failure
    }

}