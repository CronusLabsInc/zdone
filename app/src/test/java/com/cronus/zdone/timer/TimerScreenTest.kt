package com.cronus.zdone.timer

import com.cronus.zdone.FakeToaster
import com.cronus.zdone.TestTasksRepo
import org.junit.Test

class TimerScreenTest {

    val timerScreen = TimerScreen(TestTasksRepo(), TaskTimer(), FakeToaster())

    @Test
    fun `GIVEN some tasks to DO IT button THEN do nothing`() {
        timerScreen.remainingTasks = mutableListOf()
        timerScreen.startNextTask()
    }
}