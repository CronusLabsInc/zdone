package com.cronus.zdone

import io.reactivex.Scheduler

interface AppExecutors {

    fun io(): Scheduler

    fun network(): Scheduler

    fun mainThread(): Scheduler

}
