package com.cronus.zdone.timer

import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

// Class responsible for managing the data around work/rest time
// Primarily created to avoid problems with android's built-in Chronometer
class TaskTimer @Inject constructor() {
    fun of(lengthMins: Int): Observable<Long> {
        return Observable.interval(1, TimeUnit.SECONDS)
            .map { lengthMins * 60L - it }
    }
}

