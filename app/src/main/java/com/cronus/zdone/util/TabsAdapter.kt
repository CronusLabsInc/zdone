package com.cronus.zdone.util

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.wealthfront.magellan.Screen

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