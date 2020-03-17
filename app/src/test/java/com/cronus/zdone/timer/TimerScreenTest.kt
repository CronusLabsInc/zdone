package com.cronus.zdone.timer

import com.cronus.zdone.TestTasksRepo
import com.cronus.zdone.TrampolineAppExecutors
import com.cronus.zdone.api.model.Task
import org.junit.Test

class TimerScreenTest {

    val timerScreen = TimerScreen(TestTasksRepo(), TaskTimer(), TrampolineAppExecutors())

    @Test
    fun `GIVEN some tasks to DO IT button THEN do nothing`() {
        timerScreen.remainingTasks = mutableListOf()
        timerScreen.startNextTask()
    }
}