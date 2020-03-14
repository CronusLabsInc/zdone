package com.cronus.zdone

import com.cronus.zdone.api.TasksRepository
import com.cronus.zdone.api.model.UpdateDataResponse
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

    val testRepo = spyk<TasksRepository>(TestTasksRepo())

    lateinit var tasksScreen: TasksScreen

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        tasksScreen = TasksScreen(testRepo, taskTimerManager)
        tasksScreen.view = tasksView
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

        verify { testRepo.taskCompleted(eq(task.toTaskUpdateInfo())) }
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

        verify { testRepo.taskCompleted(eq(task.toTaskUpdateInfo())) }
        // check task data is refreshed
        verify { testRepo.refreshTaskData() }
    }

    @Test
    fun taskCompleted_withInProgressTask_completedOtherTask() {
        val task = DisplayedTask("fake-id", null, "Reading", "habitica", 30, false, true, WAITING)
        tasksScreen.inProgressTask =
                DisplayedTask("other_id", null, "Writing", "habitica", 30, false, true, IN_PROGRESS)
        tasksScreen.taskCompleted(task)

        verify { testRepo.taskCompleted(eq(task.toTaskUpdateInfo())) }
        assertThat(tasksScreen.inProgressTask).isNotNull()
    }

    @Test
    fun `GIVEN task is from previous day WHEN completing task THEN refresh data instead`() {
        val task = DisplayedTask("fake-id", null, "Reading", "habitica", 30, false, true, WAITING)
        every { testRepo.taskIsPreviousDay(task) } returns true
        tasksScreen.taskCompleted(task)

        verify { testRepo.refreshTaskData() }
        verify(inverse = true) { testRepo.taskCompleted(eq(task.toTaskUpdateInfo())) }
    }

    @Test
    fun taskCompleted_completedInProgressTask() {
        val task = DisplayedTask("fake-id", null, "Reading", "habitica", 30, false, true, WAITING)
        tasksScreen.inProgressTask = task
        tasksScreen.taskCompleted(task)

        verify { testRepo.taskCompleted(eq(task.toTaskUpdateInfo())) }
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

        verify { testRepo.deferTask(eq(task.toTaskUpdateInfo())) }
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

        verify { testRepo.deferTask(eq(task.toTaskUpdateInfo())) }
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

private fun DisplayedTask.toTaskUpdateInfo(): TasksRepository.TaskUpdateInfo {
    return TasksRepository.TaskUpdateInfo(id, subtaskId, service, null)
}
