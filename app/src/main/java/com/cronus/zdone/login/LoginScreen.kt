package com.cronus.zdone.login

import android.content.Context
import com.cronus.zdone.api.ApiTokenManager
import com.cronus.zdone.dagger.ScreenInjector
import com.wealthfront.magellan.Screen
import javax.inject.Inject

class LoginScreen @Inject constructor(
        val apiTokenManager: ApiTokenManager) : Screen<LoginView>() {

    override fun createView(context: Context): LoginView {
        return LoginView(context)
    }

    fun setToken(token: String) {
        apiTokenManager.putToken(token)
        navigator.replace(ScreenInjector.get().homeScreen())
    }

    override fun shouldShowActionBar() = false

    override fun shouldAnimateActionBar() = false

    fun useTestAccount() {
        setToken(ApiTokenManager.TEST_ACCOUNT_TOKEN)
    }

}
