package com.cronus.zdone.timer

import io.reactivex.Flowable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.reactive.asFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

// Class responsible for managing the data around work/rest time
// Primarily created to avoid problems with android's built-in Chronometer
class TaskTimerFactory @Inject constructor() {

    private val NANOS_PER_SEC = 1_000_000_000L

    suspend fun ofFlow(lengthMins: Int): Flow<Long> = coroutineScope {
        val startTime = System.nanoTime()
        Flowable.interval(1, TimeUnit.SECONDS)
            .map {
                val elapsedNanos = System.nanoTime() - startTime
                val elapsedSeconds = elapsedNanos / NANOS_PER_SEC
                val secondsRemaining =  lengthMins * 60 - elapsedSeconds
                secondsRemaining
            }
            .asFlow()
            .flowOn(Dispatchers.Default)
    }

}

