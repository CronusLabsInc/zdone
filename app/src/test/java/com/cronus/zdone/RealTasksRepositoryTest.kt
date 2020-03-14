package com.cronus.zdone

import com.cronus.zdone.api.TasksRepository
import com.cronus.zdone.api.RealTasksRepository
import com.cronus.zdone.api.model.Task
import com.cronus.zdone.api.model.TaskStatusUpdate
import com.cronus.zdone.home.TaskShowerStrategyProvider
import com.cronus.zdone.home.TaskShowingStrategy
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.impl.annotations.SpyK
import io.mockk.verify
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test

class RealTasksRepositoryTest {

    @SpyK
    var zdoneService = FakeZdoneService()
    @RelaxedMockK
    lateinit var taskShowerStrategyProvider: TaskShowerStrategyProvider
    @MockK
    lateinit var appExecutors: AppExecutors

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

        verify { zdoneService.updateTask(eq(TaskStatusUpdate(task.id, task.subtaskId, "complete", task.service, task.duration_seconds))) }
    }

    @Test
    fun `WHEN subtask completed THEN reports to service`() {
        val task = TasksRepository.TaskUpdateInfo("fake-id", "subtask_id", "habitica", 30)
        tasksRepository.taskCompleted(task)

        verify { zdoneService.updateTask(eq(TaskStatusUpdate(task.id, task.subtaskId, "complete", task.service, task.duration_seconds))) }
    }

    @Test
    fun `WHEN task deferred THEN reports to service`() {
        val task = TasksRepository.TaskUpdateInfo("fake-id", null, "habitica", 30)
        tasksRepository.deferTask(task)

        verify { zdoneService.updateTask(eq(TaskStatusUpdate(task.id, task.subtaskId, "defer", task.service, task.duration_seconds))) }
    }

    @Test
    fun `GIVEN cache has data WHEN refreshing THEN gets new data from the service`() {
        tasksRepository.getTasks().subscribe() // populate cache
        tasksRepository.refreshTaskData()

        verify(exactly = 2) { zdoneService.getTaskInfo() } // needs to hit service twice
    }


}