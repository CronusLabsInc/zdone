package com.cronus.zdone.home

import com.cronus.zdone.api.model.Task

class NoFilterTaskShowingStrategy : TaskShowingStrategy {
    override fun selectTasksToShow(tasks: List<Task>): List<Task> {
        return tasks
    }
}