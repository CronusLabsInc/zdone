package com.cronus.zdone

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

class TrampolineAppExecutors : AppExecutors {
    override fun io(): Scheduler {
        return Schedulers.trampoline()
    }

    override fun network(): Scheduler {
        return Schedulers.trampoline()
    }

    override fun mainThread(): Scheduler {
        return Schedulers.trampoline()
    }
}