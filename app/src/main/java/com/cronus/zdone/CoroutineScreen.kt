package com.cronus.zdone

import android.content.Context
import android.view.ViewGroup
import com.wealthfront.magellan.Screen
import com.wealthfront.magellan.ScreenView
import kotlinx.coroutines.*

abstract class CoroutineScreen<V> : Screen<V>() where V : ViewGroup, V : ScreenView<*> {

    private val scope = CoroutineScope(Dispatchers.Main)
    private val jobs = mutableSetOf<Job>()

    protected fun safeLaunch(block: suspend CoroutineScope.() -> Unit) {
        jobs.add(scope.launch { block.invoke(scope) })
    }

    override fun onHide(context: Context?) {
        super.onHide(context)
        jobs.forEach { it.cancel() }
    }
}