package com.cronus.zdone

import android.content.Context
import android.widget.Toast
import javax.inject.Inject

interface Toaster {

    fun showToast(message: String)
}

class RealToaster @Inject constructor(private val context: Context) : Toaster {

    override fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}
