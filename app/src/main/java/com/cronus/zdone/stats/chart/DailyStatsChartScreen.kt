package com.cronus.zdone.stats.chart

import android.content.Context
import android.text.Editable
import com.cronus.zdone.CoroutineScreen
import com.cronus.zdone.Preference.LongPreference.DAILY_WORK_MINS_GOAL
import com.cronus.zdone.Toaster
import com.cronus.zdone.UserPreferences
import com.cronus.zdone.stats.TaskEvent
import com.cronus.zdone.stats.TaskEventsDao
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.LocalDate
import org.joda.time.Weeks
import javax.inject.Inject

class DailyStatsChartScreen @Inject constructor(
    private val taskEventsDao: TaskEventsDao,
    private val userPrefs: UserPreferences,
    private val toaster: Toaster) :
    CoroutineScreen<DailyStatsChartView>() {

    // We are charting the current week and the 12 previous weeks of data
    // the earliest date on the chart is the 13th previous Sunday from today
    val earliestChartDay = nthPreviousSunday(13, LocalDate.now())

    override fun createView(context: Context): DailyStatsChartView = DailyStatsChartView(context)

    override fun getTitle(context: Context?) = "stats"

    override fun onShow(context: Context) {
        updateData()
    }

    private fun updateData() {
        safeLaunch {
            val dailyWorkGoalMins = userPrefs.getPref(DAILY_WORK_MINS_GOAL)
            println(dailyWorkGoalMins.toString())
            view?.setDailyWorkGoalMins(dailyWorkGoalMins)
            launch {
                taskEventsDao.getTaskEvents()
                    // note, flatmapping and working on a Flow<Task> eventually fails because you need
                    // to have a concept of when all the data is loaded. Trying to collect the Flow<Task>
                    // will fail to complete because Android's Room library doesn't send a terminal event
                    // for the flow. Thus, we do everything inside this map.
                    .map { list ->
                        list.filter { it.withinLast13Weeks() }
                            .groupBy { it.getWeekIndex() }
                            .sumTaskDurationsByWeek()
                            .toList()
                            .toMutableList()
                            .addMissingWeeks()
                            .sortedBy { it.weekIndex }
                            .map {
                                HoursWorkedData(it.second.toDouble() / (60 * 60))
                            }
                    }
                    .collect {
                        view?.setWeeklyHoursWorked(
                            it,
                            List(it.size + 1) {
                                dailyWorkGoalMins
                                    .toFloat()
                                    .toHoursInAWeek()
                            })
                    }
            }
        }
    }

    private fun TaskEvent.withinLast13Weeks(): Boolean {
        return completedAtMillis > earliestChartDay.toDateTimeAtStartOfDay().millis
    }

    private fun TaskEvent.getWeekIndex(): Int {
        val completedAtDateTime = DateTime(completedAtMillis)
        val earliestChartDayDateTime = earliestChartDay.toDateTimeAtStartOfDay()
        return Weeks.weeksBetween(earliestChartDayDateTime, completedAtDateTime).getWeeks()
    }

    private fun nthPreviousSunday(n: Int, now: LocalDate): LocalDate {
        var result = now
        val nowDayOfWeek = now.dayOfWeek

        if (nowDayOfWeek != DateTimeConstants.SUNDAY) {
            val daysUntilNextSunday = DateTimeConstants.SUNDAY - nowDayOfWeek
            // bounce result back to most recent sunday
            result = result.minusDays(DateTimeConstants.DAYS_PER_WEEK - daysUntilNextSunday)
        }
        return result.minusWeeks(n - 1) // first previous sunday is already found above
    }

    fun updateWorkGoal(updatedGoalString: String) {
        try {
            val newGoal = updatedGoalString.toLong()
            userPrefs.setPref(DAILY_WORK_MINS_GOAL, newGoal)
            updateData()
        } catch (e: NumberFormatException) {
            toaster.showToast("Invalid goal number. Please enter a valid integer")
        }
    }

}

private fun Float.toHoursInAWeek(): Float {
    return this * 7 / 60
}

// Helper functions for improving readability when processing data

private fun Map<Int, List<TaskEvent>>.sumTaskDurationsByWeek(): Map<Int, Long> {
    return mapValues { entry ->
        entry.value.map { it.durationSecs }.sum()
    }
}

private val Pair<Int, Long>.weekIndex: Int
    get() {
        return first
    }

private fun MutableList<Pair<Int, Long>>.addMissingWeeks(): List<Pair<Int, Long>> {
    // each of 0-12 should be present in the list. If missing add it in
    val weeksPresent = map { it.first }.toSet()
    (0..12).forEach {
        if (!weeksPresent.contains(it)) {
            add(it to 0)
        }
    }
    return this
}

data class HoursWorkedData(val hoursWorked: Double)



