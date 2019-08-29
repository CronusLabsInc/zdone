package com.cronus.zdone.dagger

import com.cronus.zdone.ZdoneApplication

class ScreenInjector private constructor() {

    companion object {
        @JvmStatic
        fun get(): ScreenComponent =
                ZdoneApplication.get().component.screenComponentBuilder().build()
    }
}