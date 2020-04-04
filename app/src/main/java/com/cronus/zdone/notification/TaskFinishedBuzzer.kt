package com.cronus.zdone.notification

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import javax.inject.Inject

interface TaskFinishedBuzzer {

    fun buzz()

}

class RealTaskFinishedBuzzer @Inject constructor(private val vibrator: Vibrator) : TaskFinishedBuzzer {

    override fun buzz() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(400L)
        }
    }

}
