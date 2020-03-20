package com.cronus.zdone.stats

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.joda.time.LocalDate
import javax.inject.Inject

interface DailyStatsProvider {

    val dailyStats: Flow<DailyStats>
}

data class DailyStats(
    val actualSecondsWorked: Long,
    val expectedSecondsWorked: Long,
    val numTasksCompleted: Int,
    val numTasksDeferred: Int
)

class RealDailyStatsProvider @Inject constructor(taskEventsDao: TaskEventsDao)
    : DailyStatsProvider {
    override val dailyStats: Flow<DailyStats> = taskEventsDao.getTaskEvents()
        .map { list ->
            val startOfDayMillis = LocalDate.now().toDateTimeAtStartOfDay().millis
            list.filter { event ->
                event.completedAtMillis > startOfDayMillis } }
        .map {
            val actualSecondsWorked = it.map { it.durationSecs }.sum()
            val expectedSecondsWorked = it.map { it.expectedDurationSecs }.sum()
            val tasksCompleted = it.filter { it.taskResult == TaskUpdateType.COMPLETED }.size
            val tasksDeferred = it.filter { it.taskResult == TaskUpdateType.DEFERRED }.size
            DailyStats(
                actualSecondsWorked = actualSecondsWorked,
                expectedSecondsWorked = expectedSecondsWorked,
                numTasksCompleted = tasksCompleted,
                numTasksDeferred = tasksDeferred)
        }
}
