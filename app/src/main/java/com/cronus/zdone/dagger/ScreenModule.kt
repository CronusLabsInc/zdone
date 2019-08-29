package com.cronus.zdone.dagger

import dagger.Module

@Module(subcomponents = arrayOf(
        ScreenComponent::class
))
interface ScreenModule