package com.cronus.zdone.stats.chart

import android.content.Context
import com.cronus.zdone.CoroutineScreen
import javax.inject.Inject
import kotlin.random.Random

class DailyStatsChartScreen @Inject constructor(): CoroutineScreen<DailyStatsChartView>() {

    override fun createView(context: Context): DailyStatsChartView = DailyStatsChartView(context)

    override fun getTitle(context: Context?) = "stats"

    override fun onShow(context: Context) {
        val fakeChartData = List(13) {
            HoursWorkedData(Random.nextDouble(.5, 7.5))
        }
        view?.setWeeklyHoursWorked(fakeChartData, List(14) { 5f } )
    }

}

data class HoursWorkedData(val hoursWorked: Double)



