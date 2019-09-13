package com.cronus.zdone.settings

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import com.cronus.zdone.R
import com.wealthfront.magellan.BaseScreenView
import kotlinx.android.synthetic.main.login.view.apiKeyEditText
import kotlinx.android.synthetic.main.settings.view.*

class SettingsView(context: Context) : BaseScreenView<SettingsScreen>(context) {

    private val workTimeWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            screen.udpateWorkTime(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private val apiKeyWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            screen.updateApiKey(s.toString())
        }
    }

    init {
        inflate(context, R.layout.settings, this)
        apiKeyEditText.addTextChangedListener(apiKeyWatcher)
        workTimeMinsEditText.addTextChangedListener(workTimeWatcher)
        largeFingersModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            screen.setLargeFingersModeEnabled(isChecked)
        }
        logoutButton.setOnClickListener {
            screen.logout()
        }
    }

    fun setApiKey(token: String) {
        apiKeyEditText.removeTextChangedListener(apiKeyWatcher)
        apiKeyEditText.setText(token)
        apiKeyEditText.post { apiKeyEditText.addTextChangedListener(apiKeyWatcher) }
    }

    fun setWorkTime(defaultWorkTime: Int) {
        setWorkTimeTextNoListener(defaultWorkTime.toString())
        if (workTimeMinsEditText.hasFocus()) {
            workTimeMinsEditText.post { workTimeMinsEditText.setSelection(workTimeMinsEditText.text.length) }
        }
    }

    private fun setWorkTimeTextNoListener(text: String) {
        workTimeMinsEditText.removeTextChangedListener(workTimeWatcher)
        workTimeMinsEditText.setText(text)
        workTimeMinsEditText.post { workTimeMinsEditText.addTextChangedListener(workTimeWatcher) }
    }

    fun setLargeFingersMode(enabled: Boolean) {
        largeFingersModeSwitch.isChecked = enabled
    }

    fun showLoadingWorkTime() {
        setWorkTimeTextNoListener("Loading current work time from server...")
    }

}
