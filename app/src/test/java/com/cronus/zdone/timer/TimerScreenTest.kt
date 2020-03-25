package com.cronus.zdone.timer

import com.cronus.zdone.FakeToaster
import com.cronus.zdone.TestTasksRepo
import com.cronus.zdone.api.model.Task
import com.cronus.zdone.api.model.Tasks
import com.cronus.zdone.home.RealUserSelectedTasksRepository
import com.cronus.zdone.home.UserSelectedTasksRepository
import com.cronus.zdone.stats.fake.FakeTaskEventsDao
import com.cronus.zdone.stats.summary.DailyStatsSummary
import com.cronus.zdone.stats.summary.DailyStatsSummaryProvider
import com.cronus.zdone.stats.summary.RealDailyStatsSummaryProvider
import com.google.common.truth.Truth.assertThat
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import net.danlew.android.joda.JodaTimeAndroid
import org.junit.Before
import org.junit.After
import org.junit.Test

class TimerScreenTest {

    private val testCoroutineDispactcher = TestCoroutineDispatcher()

    private val taskExecutionManager = RealTaskExecutionManager()
    private val view = mockk<TimerView>(relaxed = true)
    private val userSelectedTasksRepository = RealUserSelectedTasksRepository()
    val timerScreen = TimerScreen(TestTasksRepo(), userSelectedTasksRepository, taskExecutionManager, FakeDailyStatsProvider(), FakeToaster())

    @Before
    fun setup() {
        Dispatchers.setMain(testCoroutineDispactcher)
        timerScreen.view = view
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        testCoroutineDispactcher.cleanupTestCoroutines()
    }

    @Test
    fun `GIVEN user has selected specific tasks WHEN starting tasks THEN starts selected tasks not normal list`() = runBlockingTest {
        val selectedTask = Task(
            id = "fake-id",
            name = "Reading",
            subtasks = null,
            service = "toodledo",
            lengthMins = 15
        )

        userSelectedTasksRepository.userSelectedTask(selectedTask)
        timerScreen.startTasks()

        val taskInProgress = (taskExecutionManager.currentTaskExecutionData.first() as TaskExecutionState.TaskRunning)
            .task
        assertThat(taskInProgress.id).isEqualTo(selectedTask.id)
    }

    @Test
    fun `GIVEN working on task selected by user WHEN finishing task THEN removes selected task from list of current selected tasks`() = runBlockingTest {
        val selectedTask = Task(
            id = "fake-id",
            name = "Reading",
            subtasks = null,
            service = "toodledo",
            lengthMins = 15
        )
        userSelectedTasksRepository.userSelectedTask(selectedTask)
        timerScreen.startTasks()

        timerScreen.completeTask()

        assertThat(userSelectedTasksRepository.selectedTasks.first().isEmpty())
    }

    @Test
    fun `GIVEN working on multiple tasks selected by user WHEN finishing task THEN new task is from selected tasks`() = runBlockingTest {
        val selectedTask1 = Task(
            id = "fake-id-1",
            name = "Reading",
            subtasks = null,
            service = "toodledo",
            lengthMins = 15
        )
        val selectedTask2 = Task(
            id = "fake-id-2",
            name = "Writing",
            subtasks = null,
            service = "habitica",
            lengthMins = 25
        )
        userSelectedTasksRepository.userSelectedTask(selectedTask1)
        userSelectedTasksRepository.userSelectedTask(selectedTask2)
        timerScreen.startTasks()

        launch(testCoroutineDispactcher) {
            timerScreen.completeTask()
        }

        assertThat(userSelectedTasksRepository.selectedTasks.first()[0]).isEqualTo(selectedTask2)
        val taskInProgress = (taskExecutionManager.currentTaskExecutionData.first() as TaskExecutionState.TaskRunning)
            .task
        assertThat(taskInProgress.id).isEqualTo(selectedTask2.id)
    }


    @Test
    fun `GIVEN working on last task selected by user WHEN finishing task THEN view state becomes initial`() = runBlockingTest {
        val selectedTask = Task(
            id = "fake-id",
            name = "Reading",
            subtasks = null,
            service = "toodledo",
            lengthMins = 15
        )
        userSelectedTasksRepository.userSelectedTask(selectedTask)
        timerScreen.subscribeData()
        timerScreen.startTasks()

        launch(testCoroutineDispactcher) {
            timerScreen.completeTask()
        }

        val viewStateSlot = slot<TimerScreen.ViewState>()
        verify { view.setState(capture(viewStateSlot)) }
        println("Yay")
    }

}

private class FakeDailyStatsProvider(): DailyStatsSummaryProvider {
    override val dailyStatsSummary: Flow<DailyStatsSummary>
        get() = flowOf(
            DailyStatsSummary(
                900,
                1000,
                5,
                2
            )
        )
}
