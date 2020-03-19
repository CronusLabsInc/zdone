package com.cronus.zdone.timer

import androidx.lifecycle.Transformations.map
import io.reactivex.Flowable
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.withContext
import org.reactivestreams.Publisher
import java.util.concurrent.TimeUnit
import javax.inject.Inject

// Class responsible for managing the data around work/rest time
// Primarily created to avoid problems with android's built-in Chronometer
class TaskTimerFactory @Inject constructor() {

    suspend fun ofFlow(lengthMins: Int): Flow<Long> = coroutineScope {
        Flowable.interval(1, TimeUnit.SECONDS)
            .map { lengthMins * 60L - it }
            .asFlow()
            .flowOn(Dispatchers.IO)
    }

}

