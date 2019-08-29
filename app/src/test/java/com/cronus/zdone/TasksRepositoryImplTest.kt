package com.cronus.zdone

import com.cronus.zdone.api.TasksRepository
import com.cronus.zdone.api.TasksRepositoryImpl
import com.cronus.zdone.api.model.TaskStatusUpdate
import com.cronus.zdone.home.HomeScreen.DisplayedTask
import com.cronus.zdone.home.HomeScreen.TaskProgressState.READY
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.verify
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test

class TasksRepositoryImplTest {

    private val DEFAULT_WORK_TIME_MINS = 60

    @SpyK
    var zdoneService = TestZdoneServiceImpl()

    @MockK
    lateinit var appExecutors: AppExecutors

    @MockK
    lateinit var workTimeManager: WorkTimeManager

    lateinit var tasksRepository: TasksRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        tasksRepository = TasksRepositoryImpl(appExecutors, zdoneService, workTimeManager)
        every { appExecutors.network() } returns Schedulers.trampoline()
        every { appExecutors.mainThread() } returns Schedulers.trampoline()
        every { workTimeManager.getDefaultWorkTime() } returns DEFAULT_WORK_TIME_MINS
    }

    @Test
    fun getTasks_testCache() {
        tasksRepository.getTasks()

        verify(exactly = 1) { zdoneService.getTaskInfo(DEFAULT_WORK_TIME_MINS) }

        tasksRepository.getTasks() // returns cached data

        verify(exactly = 1) { zdoneService.getTaskInfo(DEFAULT_WORK_TIME_MINS) }
    }

    @Test
    fun getTimeData_testCache() {
        tasksRepository.getTimeData()

        verify(exactly = 1) { zdoneService.getTaskInfo(DEFAULT_WORK_TIME_MINS) }

        tasksRepository.getTimeData() // returns cached data

        verify(exactly = 1) { zdoneService.getTaskInfo(DEFAULT_WORK_TIME_MINS) }
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
        val initialResponse = tasksRepository.getTasks() // populate cache
        val refreshResponse = tasksRepository.refreshTaskData()

        verify(exactly = 2) { zdoneService.getTaskInfo(DEFAULT_WORK_TIME_MINS) } // needs to hit service twice
        assertThat(initialResponse.blockingIterable().count()).isEqualTo(1)
        assertThat(refreshResponse.blockingIterable().count()).isEqualTo(1) // should not return cached values
    }


}