package com.cronus.zdone.home

import android.content.Context
import android.view.View
import com.cronus.zdone.R
import com.cronus.zdone.util.TabsAdapter
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import com.google.android.material.tabs.TabLayout.ViewPagerOnTabSelectedListener
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import kotlinx.android.synthetic.main.tab_screen.view.*


class HomeView(context: Context, screens: List<Screen<*>>) : BaseScreenView<HomeScreen>(context) {
    fun addTabView(view: View) {
        pager.addView(view)
    }

    init {
        inflate(context, R.layout.tab_screen, this)
        initPager(context, screens)
    }

    private fun initPager(context: Context, screens: List<Screen<*>>) {
        val tabsAdapter = TabsAdapter(context, screens)
        pager.adapter = tabsAdapter
        pager.offscreenPageLimit = Integer.MAX_VALUE
        pager.addOnPageChangeListener(TabLayoutOnPageChangeListener(tabLayout))

        tabLayout.setupWithViewPager(pager)

        tabLayout.addOnTabSelectedListener(ViewPagerOnTabSelectedListener(pager))
    }

}
