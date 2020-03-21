package com.cronus.zdone.stats.chart

import android.content.Context
import com.cronus.zdone.R
import com.wealthfront.magellan.BaseScreenView

class DailyStatsChartView(context: Context) : BaseScreenView<DailyStatsChartScreen>(context) {

    init {
        inflate(context, R.layout.daily_stats_chart, this)
    }
}