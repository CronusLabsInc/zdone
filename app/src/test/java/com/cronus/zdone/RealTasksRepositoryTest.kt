package com.cronus.zdone

import com.cronus.zdone.api.TasksRepository
import com.cronus.zdone.api.RealTasksRepository
import com.cronus.zdone.api.model.Task
import com.cronus.zdone.api.model.TaskStatusUpdate
import com.cronus.zdone.home.TaskShowerStrategyProvider
import com.cronus.zdone.home.TaskShowingStrategy
import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreResponse
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.impl.annotations.SpyK
import io.mockk.verify
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class RealTasksRepositoryTest {

    @SpyK
    var zdoneService = FakeZdoneService()
    @RelaxedMockK
    lateinit var taskShowerStrategyProvider: TaskShowerStrategyProvider
    @MockK
    lateinit var appExecutors: AppExecutors

    private val testDispatcher = TestCoroutineDispatcher()

    lateinit var tasksRepository: TasksRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { appExecutors.network() } returns Schedulers.trampoline()
        every { appExecutors.mainThread() } returns Schedulers.trampoline()
        tasksRepository =
            RealTasksRepository(appExecutors, zdoneService, taskShowerStrategyProvider)
        every { taskShowerStrategyProvider.getStrategy() } returns
                object : TaskShowingStrategy {
                    override fun selectTasksToShow(tasks: List<Task>): List<Task> {
                        return tasks
                    }
                }
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    private fun testLaunch(test: suspend () -> Unit) {
        CoroutineScope(testDispatcher).launch { test.invoke() }
    }

    @Test
    fun `WHEN getting tasks from store THEN hits service`() {
        testLaunch {
            val response = tasksRepository.getTasksFromStore().toList()
            assertThat(response[0]).isEqualTo(zdoneService.tasks.tasksToDo)
        }
    }

    @Test
    fun `GIVEN store has a cache WHEN getting tasks from store THEN hits cache`() {
        testLaunch {
            tasksRepository.getTasksFromStore().toList()
            verify(exactly = 1) { zdoneService.getTaskInfo() }
            tasksRepository.getTasksFromStore().toList()
            verify(exactly = 1) { zdoneService.getTaskInfo() }
        }
    }

    @Test
    fun `GIVEN task data is cached WHEN requesting task data again THEN return cached data`() {
        // populate cache
        tasksRepository.getTasks().subscribe()

        verify(exactly = 1) { zdoneService.getTaskInfo() }

        tasksRepository.getTasks().subscribe() // returns cached data

        verify(exactly = 1) { zdoneService.getTaskInfo() }
    }

    @Test
    fun `GIVEN time data is cached WHEN requesting time data again THEN return cached data`() {
        tasksRepository.getTimeData().subscribe()

        verify(exactly = 1) { zdoneService.getTaskInfo() }

        tasksRepository.getTimeData().subscribe() // returns cached data

        verify(exactly = 1) { zdoneService.getTaskInfo() }
    }

    @Test
    fun `WHEN task completed THEN reports to service`() {
        val task = TasksRepository.TaskUpdateInfo("fake-id", null, "habitica", 30)
        tasksRepository.taskCompleted(task)

        verify {
            zdoneService.updateTask(
                eq(
                    TaskStatusUpdate(
                        task.id,
                        task.subtaskId,
                        "complete",
                        task.service,
                        task.duration_seconds
                    )
                )
            )
        }
    }

    @Test
    fun `WHEN subtask completed THEN reports to service`() {
        val task = TasksRepository.TaskUpdateInfo("fake-id", "subtask_id", "habitica", 30)
        tasksRepository.taskCompleted(task)

        verify {
            zdoneService.updateTask(
                eq(
                    TaskStatusUpdate(
                        task.id,
                        task.subtaskId,
                        "complete",
                        task.service,
                        task.duration_seconds
                    )
                )
            )
        }
    }

    @Test
    fun `WHEN task deferred THEN reports to service`() {
        val task = TasksRepository.TaskUpdateInfo("fake-id", null, "habitica", 30)
        tasksRepository.deferTask(task)

        verify {
            zdoneService.updateTask(
                eq(
                    TaskStatusUpdate(
                        task.id,
                        task.subtaskId,
                        "defer",
                        task.service,
                        task.duration_seconds
                    )
                )
            )
        }
    }

    @Test
    fun `GIVEN cache has data WHEN refreshing THEN gets new data from the service`() {
        tasksRepository.getTasks().subscribe() // populate cache
        tasksRepository.refreshTaskData()

        verify(exactly = 2) { zdoneService.getTaskInfo() } // needs to hit service twice
    }

}