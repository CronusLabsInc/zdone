package com.cronus.zdone

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkTimeManager @Inject constructor(context: Context) {

    val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = context.getSharedPreferences("zdone", Context.MODE_PRIVATE)
    }

    fun setDefaultWorkTime(timeInMins: Int) {
        sharedPreferences.edit()
                .putInt("defaultWorkTime", timeInMins)
                .apply()
    }

    fun getDefaultWorkTime(): Int {
        return sharedPreferences.getInt("defaultWorkTime", 9999)
    }


}
