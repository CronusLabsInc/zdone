package com.cronus.zdone.dagger

import android.content.Context
import com.cronus.zdone.RealToaster
import com.cronus.zdone.Toaster
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [AndroidModule.Bindings::class])
class AndroidModule(val context: Context) {

    @Provides
    fun context() = context

    @Provides
    fun sharedPreferences(context: Context) = context.getSharedPreferences("zdone", Context.MODE_PRIVATE)

    @Module
    interface Bindings {

        @Binds
        fun toaster(realToaster: RealToaster): Toaster
    }
}