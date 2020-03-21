package com.cronus.zdone.stats.chart

import android.content.Context
import com.cronus.zdone.CoroutineScreen
import javax.inject.Inject

class DailyStatsChartScreen @Inject constructor(): CoroutineScreen<DailyStatsChartView>() {

    override fun createView(context: Context): DailyStatsChartView = DailyStatsChartView(context)

    override fun getTitle(context: Context?) = "stats"

    override fun onShow(context: Context) {

    }

}




