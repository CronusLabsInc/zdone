package com.cronus.zdone

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestCoroutineDispatcher

class TestAppDispatchers : AppDispatchers {

    private val testCoroutineDispatcher = TestCoroutineDispatcher()

    override fun mainDispatcher() = testCoroutineDispatcher

    override fun backgroundDispatcher() = testCoroutineDispatcher

}