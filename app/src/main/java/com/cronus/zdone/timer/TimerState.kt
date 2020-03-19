package com.cronus.zdone.timer

import com.cronus.zdone.api.model.Task

sealed class TimerState {

    class Executing(val task: Task, val secsRemaining: Long) : TimerState()

    class CompletedTask(val task: Task) : TimerState()

    class DeferredTask(val task: Task) : TimerState()

    class Stopped() : TimerState()
}