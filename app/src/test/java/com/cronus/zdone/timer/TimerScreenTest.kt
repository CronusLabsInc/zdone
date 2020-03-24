package com.cronus.zdone.timer

import com.cronus.zdone.FakeToaster
import com.cronus.zdone.TestTasksRepo
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class TimerScreenTest {

    var taskExecutionManager = mockk<TaskExecutionManager>()
    val timerScreen = TimerScreen(TestTasksRepo(), mockk(), taskExecutionManager, mockk(), FakeToaster())

    @Test
    fun `GIVEN some tasks to DO IT button THEN do nothing`() {
        timerScreen.startTasks()
    }

}