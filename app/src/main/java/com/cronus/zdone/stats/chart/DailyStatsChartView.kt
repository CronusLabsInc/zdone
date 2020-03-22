package com.cronus.zdone.stats.chart

import android.content.Context
import com.cronus.zdone.R
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis.YAxisLabelPosition
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.wealthfront.magellan.BaseScreenView
import kotlinx.android.synthetic.main.daily_stats_chart.view.*

class DailyStatsChartView(context: Context) : BaseScreenView<DailyStatsChartScreen>(context) {

    init {
        inflate(context, com.cronus.zdone.R.layout.daily_stats_chart, this)

        daily_work_time_bar_chart.setDrawBarShadow(false)
        daily_work_time_bar_chart.setDrawValueAboveBar(false)
        daily_work_time_bar_chart.setDrawGridBackground(false)
        val hiddenDescription = Description()
        hiddenDescription.text = ""
        daily_work_time_bar_chart.description = hiddenDescription
        daily_work_time_bar_chart.legend.isEnabled = false

        val xAxis = daily_work_time_bar_chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.axisMinimum = -12.5f
        xAxis.axisMaximum = 0.5f
        xAxis.setDrawAxisLine(true)

        val leftAxis = daily_work_time_bar_chart.axisLeft
        leftAxis.setLabelCount(6, true)
        leftAxis.setPosition(YAxisLabelPosition.OUTSIDE_CHART)
        leftAxis.spaceTop = 15f
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 10f
        leftAxis.setDrawZeroLine(true)
        leftAxis.setDrawTopYLabelEntry(true)
        leftAxis.gridLineWidth = 1f

        val rightAxis = daily_work_time_bar_chart.axisRight
        rightAxis.isEnabled = false
    }

    fun setWeeklyHoursWorked(hoursWorkedData: List<HoursWorkedData>, goalLineData: List<Float>) {
        val barEntries = hoursWorkedData.mapIndexed { idx, data ->
            BarEntry(-12f + idx.toFloat(), data.hoursWorked.toFloat())
        }
        val barDataSet = ValueBasedColorBarDataSet(barEntries, "Hours worked per week over last year", goalLineData)
        barDataSet.colors = listOf(
            context.resources.getColor(R.color.colorAccent),
            context.resources.getColor(R.color.minsWorkedInWeekTooLow))
        barDataSet.setDrawValues(false)

        val lineEntries = goalLineData.mapIndexed { idx, data ->
            Entry(-12.5f + idx.toFloat(), data) }
        val lineDataSet = LineDataSet(lineEntries, "Goal hours per week")
        lineDataSet.setDrawCircles(false)
        lineDataSet.setDrawValues(false)
        lineDataSet.lineWidth = 3f
        lineDataSet.color = context.resources.getColor(R.color.minsWorkedInWeekTarget)


        val combinedData = CombinedData()
        combinedData.setData(BarData(barDataSet))
        combinedData.setData(LineData(lineDataSet))
        daily_work_time_bar_chart.data = combinedData
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