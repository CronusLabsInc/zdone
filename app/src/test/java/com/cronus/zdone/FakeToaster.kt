package com.cronus.zdone

class FakeToaster : Toaster {

    var lastMessage: String? = null

    override fun showToast(message: String) {
        lastMessage = message
    }

}