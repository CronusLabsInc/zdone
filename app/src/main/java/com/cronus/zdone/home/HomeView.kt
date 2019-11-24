package com.cronus.zdone.home

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import com.google.android.material.tabs.TabLayout.ViewPagerOnTabSelectedListener
import com.wealthfront.magellan.BaseScreenView
import com.wealthfront.magellan.Screen
import kotlinx.android.synthetic.main.home.view.*


class HomeView(context: Context, screens: List<Screen<*>>) : BaseScreenView<HomeScreen>(context) {
    fun addTabView(view: View) {
        pager.addView(view)
    }

    init {
        inflate(context, com.cronus.zdone.R.layout.home, this)
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

    class TabsAdapter(val context: Context, val tabScreens: List<Screen<*>>) : PagerAdapter() {

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            return tabScreens.get(position).getView()
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun getCount(): Int {
            return tabScreens.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return tabScreens[position].getTitle(context)
        }

    }
}
