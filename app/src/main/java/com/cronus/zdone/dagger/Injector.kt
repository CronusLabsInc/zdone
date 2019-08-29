package com.cronus.zdone.dagger

import com.cronus.zdone.ZdoneApplication

class Injector private constructor() {

    companion object {
        @JvmStatic
        fun get(): AppComponent = ZdoneApplication.get().component
    }
}