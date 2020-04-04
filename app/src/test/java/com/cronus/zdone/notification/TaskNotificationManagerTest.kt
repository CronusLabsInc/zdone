package com.cronus.zdone.notification

import com.cronus.zdone.api.model.Task
import com.cronus.zdone.timer.TaskExecutionState
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class TaskNotificationManagerTest {

    private val notificationShower = FakeNotificationShower()
    private val buzzer = FakeTaskFinishedBuzzer()
    private var taskNotificationManager = TaskNotificationManager(notificationShower, buzzer)

    @Before
    fun setup() {
        notificationShower.isNotificationShowing = false
        buzzer.buzzed = false
    }

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

    @Test
    fun `GIVEN no time remaining in task WHEN receiving task data THEN buzz`() {
        val exampleTask = Task("fake-id", "reading", null, "habitica", 30)
        taskNotificationManager.handleTaskExecutionState(TaskExecutionState.TaskRunning(exampleTask, 0))

        assertThat(buzzer.buzzed).isTrue()
    }

    @Test
    fun `GIVEN some time remaining in task WHEN receiving task data THEN don't buzz`() {
        val exampleTask = Task("fake-id", "reading", null, "habitica", 30)
        taskNotificationManager.handleTaskExecutionState(TaskExecutionState.TaskRunning(exampleTask, 1))

        assertThat(buzzer.buzzed).isFalse()
    }

}

class FakeTaskFinishedBuzzer : TaskFinishedBuzzer {
    var buzzed = false

    override fun buzz() {
        buzzed = true
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
