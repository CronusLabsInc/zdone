package com.cronus.zdone

import com.cronus.zdone.api.TasksRepository
import com.cronus.zdone.api.model.Task
import com.cronus.zdone.api.model.UpdateDataResponse
import com.cronus.zdone.home.TaskShowerStrategyProvider
import com.cronus.zdone.home.TaskShowingStrategy
import com.cronus.zdone.home.TasksScreen
import com.cronus.zdone.home.TasksScreen.DisplayedTask
import com.cronus.zdone.home.TasksScreen.TaskProgressState.*
import com.cronus.zdone.home.TasksView
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

class TasksScreenTest {

    @RelaxedMockK
    lateinit var taskTimerManager: TaskTimerManager
    @RelaxedMockK
    lateinit var tasksView: TasksView
    @RelaxedMockK
    lateinit var taskShowerStrategyProvider: TaskShowerStrategyProvider

    val testRepo = spyk<TasksRepository>(TestTasksRepo())

    lateinit var tasksScreen: TasksScreen

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        tasksScreen = TasksScreen(testRepo, taskTimerManager, taskShowerStrategyProvider)
        tasksScreen.view = tasksView
        every { taskShowerStrategyProvider.getStrategy() } returns
                object : TaskShowingStrategy {
                    override fun selectTasksToShow(tasks: List<Task>): List<Task> {
                        return tasks
                    }
                }
    }

    @Test
    fun requestTaskData() {
        tasksScreen.requestTaskData()
        val displayedTasks = tasksScreen.getDisplayedTasks(testRepo.getTasks().blockingFirst())

        verify { tasksView.setTasks(eq(displayedTasks)) }
        verify { tasksView.setTimeProgress(85) }
    }

    @Test
    fun taskCompleted_noInProgressTask() {
        val task = DisplayedTask("fake-id", null, "Reading", "habitica", 30, false, true, READY)
        tasksScreen.taskCompleted(task)

        verify { testRepo.taskCompleted(task) }
    }

    @Test
    fun taskCompleted_onFailure() {
        every { testRepo.taskCompleted(any()) } returns Observable.just(
            UpdateDataResponse(
                        "failure",
                        "500 internal server error"
                )
        )
        val task = DisplayedTask("fake-id", null, "Reading", "habitica", 30, false, true, READY)
        tasksScreen.taskCompleted(task)

        verify { testRepo.taskCompleted(task) }
        // check task data is refreshed
        verify { testRepo.refreshTaskData() }
    }

    @Test
    fun taskCompleted_withInProgressTask_completedOtherTask() {
        val task = DisplayedTask("fake-id", null, "Reading", "habitica", 30, false, true, WAITING)
        tasksScreen.inProgressTask =
                DisplayedTask("other_id", null, "Writing", "habitica", 30, false, true, IN_PROGRESS)
        tasksScreen.taskCompleted(task)

        verify { testRepo.taskCompleted(task) }
        assertThat(tasksScreen.inProgressTask).isNotNull()
    }

    @Test
    fun taskCompleted_completedInProgressTask() {
        val task = DisplayedTask("fake-id", null, "Reading", "habitica", 30, false, true, WAITING)
        tasksScreen.inProgressTask = task
        tasksScreen.taskCompleted(task)

        verify { testRepo.taskCompleted(task) }
        assertThat(tasksScreen.inProgressTask).isNull()
        verify { taskTimerManager.cancelTimer() }
        verify { tasksView.setTasksProgressState(READY) }
    }

    @Test
    fun startTask() {
        val task = DisplayedTask("fake-id", null, "Reading", "habitica", 30, false, true, READY)
        tasksScreen.startTask(task)

        assertThat(tasksScreen.inProgressTask).isEqualTo(task)
        verify { taskTimerManager.startTimer(eq(task)) }
        verify { tasksView.setInProgressTask(eq(task)) }
    }

    @Test
    fun pauseTask() {
        val task = DisplayedTask("fake-id", null, "Reading", "habitica", 30, false, true, READY)
        tasksScreen.inProgressTask = task
        tasksScreen.pauseTask(task)

        assertThat(tasksScreen.inProgressTask).isNull()
        verify { taskTimerManager.cancelTimer() }
        verify { tasksView.setTasksProgressState(READY) }
    }

    @Test
    fun deferTask_success() {
        val task = DisplayedTask("fake-id", null, "Reading", "habitica", 30, false, true, READY)
        tasksScreen.deferTask(task)

        verify { testRepo.deferTask(task) }
    }

    @Test
    fun deferTask_failure() {
        val task = DisplayedTask("fake-id", null, "Reading", "habitica", 30, false, true, READY)
        every { testRepo.deferTask(any()) } returns Observable.just(
            UpdateDataResponse(
                        "error",
                        "500 internal server error"
                )
        )
        tasksScreen.deferTask(task)

        verify { testRepo.deferTask(task) }
        verify { testRepo.refreshTaskData() } // should re-request task data on failure
    }

    @Test
    fun deferTask_apiError() {
        val task = DisplayedTask("fake-id", null, "Reading", "habitica", 30, false, true, READY)
        every { testRepo.deferTask(any()) } returns Observable.error(Exception("boom"))
        tasksScreen.deferTask(task)

        verify { tasksView.showError("boom") } // should re-request task data on failure
    }

}