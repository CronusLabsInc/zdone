package com.cronus.zdone.home

import com.cronus.zdone.api.model.Task

interface TaskShowingStrategy {

    fun selectTasksToShow(tasks: List<Task>): List<Task>

}
