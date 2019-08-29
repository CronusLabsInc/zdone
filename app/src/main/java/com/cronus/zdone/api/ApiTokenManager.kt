package com.cronus.zdone.api

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiTokenManager @Inject constructor(context: Context) {

    companion object {
        const val TEST_ACCOUNT_TOKEN = "07360bb7-89e2-44e0-add6-2ba723c11370"
    }

    val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = context.getSharedPreferences("zdone", Context.MODE_PRIVATE)
    }

    private val TOKEN_KEY = "token"

    fun putToken(token: String) {
        sharedPreferences.edit()
                .putString(TOKEN_KEY, token)
                .apply()
    }

    fun getToken(): String {
        return sharedPreferences.getString(TOKEN_KEY, "")!!
    }

    fun hasToken(): Boolean {
        return getToken().isNotEmpty()
    }

}
