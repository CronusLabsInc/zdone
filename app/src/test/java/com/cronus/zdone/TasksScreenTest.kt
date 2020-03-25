package com.cronus.zdone

import com.cronus.zdone.api.TasksRepository
import com.cronus.zdone.api.model.Task
import com.cronus.zdone.api.model.UpdateDataResponse
import com.cronus.zdone.home.TaskShowerStrategyProvider
import com.cronus.zdone.home.TasksScreen
import com.cronus.zdone.home.TasksScreen.DisplayedTask
import com.cronus.zdone.home.TasksScreen.TaskProgressState.*
import com.cronus.zdone.home.TasksView
import com.cronus.zdone.home.UserSelectedTasksRepository
import com.cronus.zdone.stats.TaskUpdateType
import com.cronus.zdone.timer.TaskExecutionManager
import com.dropbox.android.external.store4.ResponseOrigin
import com.dropbox.android.external.store4.StoreResponse
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class TasksScreenTest {

    private val testDispatcher = TestCoroutineDispatcher()

    @RelaxedMockK
    lateinit var taskTimerManager: TaskExecutionManager
    @RelaxedMockK
    lateinit var tasksView: TasksView
    @RelaxedMockK
    lateinit var userSelectedTasksRepository: UserSelectedTasksRepository
    @RelaxedMockK
    lateinit var taskShowerStrategyProvider: TaskShowerStrategyProvider

    val testRepo = spyk<TasksRepository>(TestTasksRepo())

    lateinit var tasksScreen: TasksScreen

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this, relaxUnitFun = true)
        tasksScreen = TasksScreen(testRepo, userSelectedTasksRepository, taskTimerManager, FakeToaster(), taskShowerStrategyProvider)
        tasksScreen.view = tasksView
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun requestTaskData() = runBlockingTest {
        tasksScreen.requestTaskData()
        val tasks = testRepo.getTasksFromStore().toList()[0].dataOrNull()!!
        val displayedTasks = tasksScreen.getDisplayedTasks(tasks)
        verify { tasksView.setTasks(eq(displayedTasks)) }
        verify { tasksView.setTimeProgress(85) }
    }

    @Test
    fun taskCompleted_noInProgressTask() {
        val task = DisplayedTask(
            "fake-id",
            null,
            "Reading",
            "habitica",
            30,
            false,
            true,
            READY,
            isSelected = false)
        tasksScreen.taskCompleted(task)

        coVerify { testRepo.updateTask(eq(task.toTaskUpdateInfo(TaskUpdateType.COMPLETED))) }
    }

    @Test
    fun taskCompleted_onFailure() {
        coEvery { testRepo.updateTask(any()) } returns flowOf(
            StoreResponse.Data(
                UpdateDataResponse(
                    "failure",
                    "500 internal server error"
                ), ResponseOrigin.Fetcher
            )
        )
        val task = DisplayedTask("fake-id", null, "Reading", "habitica", 30, false, true, READY, isSelected = false)
        tasksScreen.taskCompleted(task)

        coVerify { testRepo.updateTask(eq(task.toTaskUpdateInfo(TaskUpdateType.COMPLETED))) }
        // check task data is refreshed
        coVerify { testRepo.refreshTaskDataFromStore() }
    }

    @Test
    fun taskCompleted_withInProgressTask_completedOtherTask() {
        val task = DisplayedTask("fake-id", null, "Reading", "habitica", 30, false, true, WAITING, isSelected = false)
        tasksScreen.inProgressTask =
            DisplayedTask("other_id", null, "Writing", "habitica", 30, false, true, IN_PROGRESS, isSelected = false)
        tasksScreen.taskCompleted(task)

        coVerify { testRepo.updateTask(eq(task.toTaskUpdateInfo(TaskUpdateType.COMPLETED))) }
        assertThat(tasksScreen.inProgressTask).isNotNull()
    }

    @Test
    fun `GIVEN tasks are from previous day WHEN completing task THEN refresh data instead`() {
        val task = DisplayedTask("fake-id", null, "Reading", "habitica", 30, false, true, WAITING, isSelected = false)
        every { testRepo.areTasksFromPreviousDay() } returns true
        tasksScreen.taskCompleted(task)

        coVerify { testRepo.refreshTaskDataFromStore() }
        coVerify(inverse = true) { testRepo.updateTask(eq(task.toTaskUpdateInfo(TaskUpdateType.COMPLETED))) }
    }

    @Test
    fun taskCompleted_completedInProgressTask() {
        val task = DisplayedTask("fake-id", null, "Reading", "habitica", 30, false, true, WAITING, isSelected = false)
        tasksScreen.inProgressTask = task
        tasksScreen.taskCompleted(task)

        coVerify { testRepo.updateTask(eq(task.toTaskUpdateInfo(TaskUpdateType.COMPLETED))) }
        assertThat(tasksScreen.inProgressTask).isNull()
        verify { taskTimerManager.cancelTasks() }
        verify { tasksView.setTasksProgressState(READY) }
    }

    @Test
    fun startTask() = testDispatcher.runBlockingTest {
        val task = testRepo.getTasksFromStore().toList()[0].dataOrNull()!![0] // yeah...
        val displayedTask = tasksScreen.getDisplayedTasks(listOf(task))[0]
        tasksScreen.startTask(displayedTask)

        assertThat(tasksScreen.inProgressTask).isEqualTo(displayedTask)
        val tasksStarted = slot<List<Task>>()
        coVerify { taskTimerManager.startTasks(capture(tasksStarted)) }
        assertThat(tasksStarted.captured).containsExactly(task)
        verify { tasksView.setInProgressTask(eq(displayedTask)) }
    }

    @Test
    fun pauseTask() {
        val task = DisplayedTask("fake-id", null, "Reading", "habitica", 30, false, true, READY, isSelected = false)
        tasksScreen.inProgressTask = task
        tasksScreen.pauseTask(task)

        assertThat(tasksScreen.inProgressTask).isNull()
        verify { taskTimerManager.cancelTasks() }
        verify { tasksView.setTasksProgressState(READY) }
    }

    @Test
    fun deferTask_success() {
        val task = DisplayedTask("fake-id", null, "Reading", "habitica", 30, false, true, READY, isSelected = false)
        tasksScreen.deferTask(task)

        coVerify { testRepo.updateTask(eq(task.toTaskUpdateInfo(TaskUpdateType.DEFERRED))) }
    }

    @Test
    fun deferTask_failure() {
        val task = DisplayedTask("fake-id", null, "Reading", "habitica", 30, false, true, READY, isSelected = false)
        coEvery { testRepo.updateTask(any()) } returns flowOf(
            StoreResponse.Data(
                UpdateDataResponse(
                    "error",
                    "500 internal server error"
                ), ResponseOrigin.Fetcher
            )
        )

        tasksScreen.deferTask(task)

        coVerify { testRepo.updateTask(eq(task.toTaskUpdateInfo(TaskUpdateType.DEFERRED))) }
        coVerify { testRepo.refreshTaskDataFromStore() } // should re-request task data on failure
    }

    @Test
    fun deferTask_apiError() {
        val task = DisplayedTask("fake-id", null, "Reading", "habitica", 30, false, true, READY, isSelected = false)
        coEvery { testRepo.updateTask(any()) } returns flowOf(
            StoreResponse.Error(
                Exception("boom"),
                ResponseOrigin.Fetcher
            )
        )
        tasksScreen.deferTask(task)

        coVerify { tasksView.showError("boom") } // should re-request task data on failure
        coVerify { testRepo.refreshTaskDataFromStore() }
    }

}

private fun DisplayedTask.toTaskUpdateInfo(updateType: TaskUpdateType): TasksRepository.TaskUpdateInfo {
    return TasksRepository.TaskUpdateInfo(
        id = id,
        name = name,
        subtaskId = subtaskId,
        service = service,
        expectedDurationSeconds = lengthMins * 60L,
        actualDurationSeconds = lengthMins * 60L,
        updateType = updateType)
}
