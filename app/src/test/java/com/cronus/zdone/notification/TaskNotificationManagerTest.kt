package com.cronus.zdone.notification

import com.cronus.zdone.api.model.Task
import com.cronus.zdone.timer.FakeTaskExecutionManager
import com.cronus.zdone.timer.TaskExecutionState
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

class TaskNotificationManagerTest {

    private val notificationShower = FakeNotificationShower()
    private var taskNotificationManager = TaskNotificationManager(notificationShower)

    @Test
    fun `GIVEN waiting for tasks WHEN receiving task state THEN clear notifications`() {
        taskNotificationManager.handleTaskExecutionState(TaskExecutionState.WaitingForTasks)

        assertThat(notificationShower.isNotificationShowing).isEqualTo(false)
    }

    @Test
    fun `GIVEN state is all tasks completed WHEN receiving task state THEN clear notifications`() {
        taskNotificationManager.handleTaskExecutionState(TaskExecutionState.AllTasksCompleted)

        assertThat(notificationShower.isNotificationShowing).isEqualTo(false)
    }

    @Test
    fun `GIVEN state is task running WHEN receiving task state THEN show notifications`() {
        val exampleTask = Task("fake-id", "reading", null, "habitica", 30)
        taskNotificationManager.handleTaskExecutionState(TaskExecutionState.TaskRunning(exampleTask, 0))

        assertThat(notificationShower.isNotificationShowing).isEqualTo(true)
    }

}

class FakeNotificationShower : TaskNotificationShower {

    var isNotificationShowing = false

    override fun showNotification() {
        isNotificationShowing = true
    }

    override fun hideNotification() {
        isNotificationShowing = false
    }

}
