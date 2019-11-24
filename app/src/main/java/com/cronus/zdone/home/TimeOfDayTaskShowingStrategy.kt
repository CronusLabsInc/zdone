package com.cronus.zdone.home

import android.util.Log
import com.cronus.zdone.api.model.Task
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

class TimeOfDayTaskShowingStrategy : TaskShowingStrategy {

    private enum class SectionOfDay {
        MORNING {
            override fun filterTasks(tasks: List<Task>): List<Task> {
                val morningTasks = tasks.filter { it.name.startsWith("M+") }
                return morningTasks
            }
        },
        DAY {
            override fun filterTasks(tasks: List<Task>): List<Task> {
                val morningTasks = MORNING.filterTasks(tasks)
                val todayTasks = tasks.filter { !(it.name.startsWith("M+") || it.name.startsWith("N+")) }
                val tasksRemaining = mutableListOf(todayTasks, morningTasks).flatten()
                if (tasksRemaining.isEmpty()) {
                    return EVENING.filterTasks(tasks)
                } else {
                    return tasksRemaining
                }
            }
        },
        EVENING {
            override fun filterTasks(tasks: List<Task>): List<Task> {
                val eveningTasks = tasks.filter { it.name.startsWith("N+") }
                if (eveningTasks.isEmpty()) {
                    return tasks
                } else {
                    return eveningTasks
                }
            }
        };

        abstract fun filterTasks(tasks: List<Task>): List<Task>
    }


    override fun selectTasksToShow(tasks: List<Task>): List<Task> {
        val currentDaySection = DateTime.now(DateTimeZone.getDefault()).getSectionOfDay()
        Log.d("ZDONE", "current timezone: ${DateTimeZone.getDefault()}")
        Log.d("ZDONE", "currentDaySection: $currentDaySection")
        return currentDaySection.filterTasks(tasks)
    }


    private fun DateTime.getSectionOfDay() = when {
        hourOfDay().get() <= 8 -> SectionOfDay.MORNING
        hourOfDay().get() <= 19 -> SectionOfDay.DAY
        else -> SectionOfDay.EVENING
    }
}

