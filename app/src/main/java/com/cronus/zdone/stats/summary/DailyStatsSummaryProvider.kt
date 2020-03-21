package com.cronus.zdone.stats.summary

import com.cronus.zdone.stats.TaskEventsDao
import com.cronus.zdone.stats.TaskUpdateType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.joda.time.LocalDate
import javax.inject.Inject

interface DailyStatsSummaryProvider {

    val dailyStatsSummary: Flow<DailyStatsSummary>
}

data class DailyStatsSummary(
    val actualSecondsWorked: Long,
    val expectedSecondsWorked: Long,
    val numTasksCompleted: Int,
    val numTasksDeferred: Int
)

class RealDailyStatsSummaryProvider @Inject constructor(taskEventsDao: TaskEventsDao)
    : DailyStatsSummaryProvider {
    override val dailyStatsSummary: Flow<DailyStatsSummary> = taskEventsDao.getTaskEvents()
        .map { list ->
            val startOfDayMillis = LocalDate.now().toDateTimeAtStartOfDay().millis
            list.filter { event ->
                event.completedAtMillis > startOfDayMillis } }
        .map {
            val actualSecondsWorked = it.map { it.durationSecs }.sum()
            val expectedSecondsWorked = it.map { it.expectedDurationSecs }.sum()
            val tasksCompleted = it.filter { it.taskResult == TaskUpdateType.COMPLETED }.size
            val tasksDeferred = it.filter { it.taskResult == TaskUpdateType.DEFERRED }.size
            DailyStatsSummary(
                actualSecondsWorked = actualSecondsWorked,
                expectedSecondsWorked = expectedSecondsWorked,
                numTasksCompleted = tasksCompleted,
                numTasksDeferred = tasksDeferred)
        }
}
