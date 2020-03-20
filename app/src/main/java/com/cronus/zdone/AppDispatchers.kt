package com.cronus.zdone

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

interface AppDispatchers {

    fun mainDispatcher(): CoroutineDispatcher

    fun backgroundDispatcher(): CoroutineDispatcher

}

@Singleton
class RealAppDispatchers @Inject constructor() : AppDispatchers {

    override fun mainDispatcher() = Dispatchers.Main

    override fun backgroundDispatcher() = Dispatchers.IO

}
