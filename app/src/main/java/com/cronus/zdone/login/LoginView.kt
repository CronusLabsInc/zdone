package com.cronus.zdone.login

import android.content.Context
import android.view.View
import com.cronus.zdone.R
import com.wealthfront.magellan.BaseScreenView
import kotlinx.android.synthetic.main.login.view.*

class LoginView(context: Context) : BaseScreenView<LoginScreen>(context) {

    init {
        View.inflate(context, R.layout.login, this)
        submitApiKeyButton.setOnClickListener {
            screen.setToken(apiKeyEditText.text.toString())
        }
        useTestAccountButton.setOnClickListener {
            screen.useTestAccount()
        }
    }

}
