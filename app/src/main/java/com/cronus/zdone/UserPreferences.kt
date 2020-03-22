package com.cronus.zdone

import android.content.SharedPreferences
import com.cronus.zdone.util.Do
import javax.inject.Inject

interface UserPreferences {

    fun <T> getPref(pref: Preference<T>): T

    fun <T> setPref(pref: Preference<T>, newVal: T)

}

sealed class Preference<T>(val key: String, val defaultValue: T) {
    sealed class LongPreference(key: String, defaultValue: Long) : Preference<Long>(key, defaultValue) {
        object DAILY_WORK_MINS_GOAL : LongPreference("DAILY_WORK_MINS_GOAL", 30)
    }
}

// Meant as simple wrapper on SharedPreferences to enable easier testing
class RealUserPreferences @Inject constructor(private val sharedPreferences: SharedPreferences): UserPreferences {

    override fun <T> getPref(pref: Preference<T>): T {
        return when (pref) {
            is Preference.LongPreference -> sharedPreferences.getLong(pref.key, pref.defaultValue)
        } as T
    }

    override fun <T> setPref(pref: Preference<T>, newVal: T) {
        Do exhaustive when (pref) {
            is Preference.LongPreference -> sharedPreferences.edit().putLong(pref.key, newVal as Long).apply()
        }
    }

}