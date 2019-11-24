package com.cronus.zdone.dagger

import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class AndroidModule(val context: Context) {

    @Provides
    fun context() = context

    @Provides
    fun sharedPreferences(context: Context) = context.getSharedPreferences("zdone", Context.MODE_PRIVATE)
}