package com.cronus.zdone.settings

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import com.cronus.zdone.R
import com.wealthfront.magellan.BaseScreenView
import kotlinx.android.synthetic.main.login.view.apiKeyEditText
import kotlinx.android.synthetic.main.settings.view.*

class SettingsView(context: Context) : BaseScreenView<SettingsScreen>(context) {

    init {
        inflate(context, R.layout.settings, this)
        apiKeyEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                screen.updateApiKey(s.toString())
            }

        })
        workTimeMinsEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                screen.udpateWorkTime(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        logoutButton.setOnClickListener {
            screen.logout()
        }
    }

    fun setApiKey(token: String) {
        apiKeyEditText.setText(token)
    }

    fun setWorkTime(defaultWorkTime: Int) {
        workTimeMinsEditText.setText(defaultWorkTime.toString())
        if (workTimeMinsEditText.hasFocus()) {
            workTimeMinsEditText.setSelection(workTimeMinsEditText.text.length)
        }
    }

}
