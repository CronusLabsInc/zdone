package com.cronus.zdone

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppExecutorsImpl @Inject constructor() : AppExecutors {

    companion object {
        private var lastNetworkUsed = 0;
        private val NETWORK_THREADS = 8
        private val networkSchedulers = Array(NETWORK_THREADS) { Schedulers.newThread() }
    }

    override fun io(): Scheduler {
        return Schedulers.io()
    }

    override fun network(): Scheduler {
        return networkSchedulers[lastNetworkUsed++ % NETWORK_THREADS]
    }

    override fun mainThread(): Scheduler {
        return AndroidSchedulers.mainThread()
    }
}