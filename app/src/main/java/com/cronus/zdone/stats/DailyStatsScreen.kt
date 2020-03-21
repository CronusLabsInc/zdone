package com.cronus.zdone.stats

import android.content.Context
import android.view.View
import com.cronus.zdone.R
import com.cronus.zdone.stats.chart.DailyStatsChartScreen
import com.cronus.zdone.stats.log.DailyStatsLogScreen
import com.cronus.zdone.util.TabsAdapter
import com.google.android.material.tabs.TabLayout
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import com.wealthfront.magellan.ScreenGroup
import kotlinx.android.synthetic.main.tab_screen.view.*
import javax.inject.Inject

class DailyStatsScreen @Inject constructor(chartScreen: DailyStatsChartScreen, logScreen: DailyStatsLogScreen): ScreenGroup<Screen<*>, DailyStatsView>(
    listOf(chartScreen, logScreen)) {

    override fun createView(context: Context): DailyStatsView = DailyStatsView(context, screens)

    override fun onShow(context: Context?) {
        super.onShow(context)
        screens.map {
            view.addTabView(it.getView())
        }
    }

    override fun getTitle(context: Context?) = "zdone"

}

class DailyStatsView(context: Context, screens: List<Screen<*>>) : BaseScreenView<DailyStatsScreen>(context) {

    init {
        inflate(context, R.layout.tab_screen, this)
        initPager(context, screens)
    }

    fun addTabView(view: View) {
        pager.addView(view)
    }

    private fun initPager(context: Context, screens: List<Screen<*>>) {
        val tabsAdapter = TabsAdapter(context, screens)
        pager.adapter = tabsAdapter
        pager.offscreenPageLimit = Integer.MAX_VALUE
        pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        tabLayout.setupWithViewPager(pager)

        tabLayout.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(pager))
    }

}

