package com.cronus.zdone.home

import android.content.Context
import com.cronus.zdone.R
import com.wealthfront.magellan.BaseScreenView

class TimerView(context: Context) : BaseScreenView<TimerScreen>(context) {

    init {
        inflate(context, R.layout.timer, this)
    }

}
