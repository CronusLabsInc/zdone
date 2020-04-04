package com.cronus.zdone.timer

import android.util.Log
import io.reactivex.Flowable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

// Class responsible for managing the data around work/rest time
// Primarily created to avoid problems with android's built-in Chronometer
class TaskTimerFactory @Inject constructor() {

    private val MILLIS_PER_SEC = 1_000L

    suspend fun ofFlow(lengthMins: Int): Flow<Long> = coroutineScope {
        withContext(Dispatchers.Default) {
            val startTime = System.currentTimeMillis()
            Flowable.interval(1, TimeUnit.SECONDS)
                .map {
                    val elapsedMillis = System.currentTimeMillis() - startTime
                    val elapsedSeconds = elapsedMillis / MILLIS_PER_SEC
                    val secondsRemaining =  lengthMins * 60 - elapsedSeconds
                    secondsRemaining
                }
                .asFlow()
        }
    }

}

