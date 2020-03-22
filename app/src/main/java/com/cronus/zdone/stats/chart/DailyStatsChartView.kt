package com.cronus.zdone.stats.chart

import android.content.Context
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Toast
import com.cronus.zdone.R
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.wealthfront.magellan.BaseScreenView
import kotlinx.android.synthetic.main.daily_stats_chart.view.*

class DailyStatsChartView(context: Context) : BaseScreenView<DailyStatsChartScreen>(context) {

    init {
        inflate(context, R.layout.daily_stats_chart, this)
        time_granularity.adapter = ArrayAdapter.createFromResource(context, R.array.time_granularity_options, android.R.layout.simple_spinner_item)
        daily_work_goal_mins.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                screen.updateWorkGoal(daily_work_goal_mins.text.toString())
                daily_work_goal_mins.clearFocus()
            }
            false
        }
    }

    fun setWeeklyHoursWorked(hoursWorkedData: List<HoursWorkedData>, goalLineData: List<Float>) {
        time_worked_bar_chart.setData(hoursWorkedData, goalLineData)
    }

    fun setDailyWorkGoalMins(dailyMinsGoal: Long) {
        daily_work_goal_mins.setText(dailyMinsGoal.toString())
    }
}

class ValueBasedColorBarDataSet(yVals: List<BarEntry>, label: String, private val goals: List<Float>) : BarDataSet(yVals, label) {

    override fun getColor(idx: Int): Int {
        return if (this.getEntryForIndex(idx).y > goals[idx]) colors[0] else colors[1]
    }

    override fun getEntryIndex(e: BarEntry?): Int {
        return super<BarDataSet>.getEntryIndex(e)
    }

}