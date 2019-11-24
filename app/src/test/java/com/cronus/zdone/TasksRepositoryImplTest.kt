package com.cronus.zdone

import com.cronus.zdone.api.TasksRepository
import com.cronus.zdone.api.TasksRepositoryImpl
import com.cronus.zdone.api.model.Task
import com.cronus.zdone.api.model.TaskStatusUpdate
import com.cronus.zdone.home.TaskShowerStrategyProvider
import com.cronus.zdone.home.TaskShowingStrategy
import com.cronus.zdone.home.TasksScreen.DisplayedTask
import com.cronus.zdone.home.TasksScreen.TaskProgressState.READY
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.impl.annotations.SpyK
import io.mockk.verify
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test

class TasksRepositoryImplTest {

    @SpyK
    var zdoneService = TestZdoneServiceImpl()
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
            TasksRepositoryImpl(appExecutors, zdoneService, taskShowerStrategyProvider)
        every { taskShowerStrategyProvider.getStrategy() } returns
                object : TaskShowingStrategy {
                    override fun selectTasksToShow(tasks: List<Task>): List<Task> {
                        return tasks
                    }
                }
    }

    @Test
    fun getTasks_testCache() {
        tasksRepository.getTasks().subscribe()

        verify(exactly = 1) { zdoneService.getTaskInfo() }

        tasksRepository.getTasks().subscribe() // returns cached data

        verify(exactly = 1) { zdoneService.getTaskInfo() }
    }

    @Test
    fun getTimeData_testCache() {
        tasksRepository.getTimeData().subscribe()

        verify(exactly = 1) { zdoneService.getTaskInfo() }

        tasksRepository.getTimeData().subscribe() // returns cached data

        verify(exactly = 1) { zdoneService.getTaskInfo() }
    }

    @Test
    fun taskCompleted_topLevelTask() {
        val task = DisplayedTask("fake-id", null, "studying", "habitica", 30, false, true, READY)
        tasksRepository.taskCompleted(task)

        verify { zdoneService.updateTask(eq(TaskStatusUpdate(task.id, task.subtaskId, "complete", task.service))) }
    }

    @Test
    fun taskCompleted_subTask() {
        val task = DisplayedTask("fake-id", "subtask_id", "studying", "habitica", 30, false, true, READY)
        tasksRepository.taskCompleted(task)

        verify { zdoneService.updateTask(eq(TaskStatusUpdate(task.id, task.subtaskId, "complete", task.service))) }
    }

    @Test
    fun taskDeferred() {
        val task = DisplayedTask("fake-id", null, "studying", "habitica", 30, false, true, READY)
        tasksRepository.deferTask(task)

        verify { zdoneService.updateTask(eq(TaskStatusUpdate(task.id, task.subtaskId, "defer", task.service))) }
    }

    @Test
    fun refreshTaskData() {
        tasksRepository.getTasks().subscribe() // populate cache
        tasksRepository.refreshTaskData()

        verify(exactly = 2) { zdoneService.getTaskInfo() } // needs to hit service twice
    }


}