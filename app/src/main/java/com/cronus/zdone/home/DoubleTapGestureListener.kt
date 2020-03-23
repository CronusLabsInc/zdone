package com.cronus.zdone.home

import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat

class DoubleTapGestureListener(private val doubleTapListener: () -> Unit) : GestureDetector.SimpleOnGestureListener() {

    override fun onDown(e: MotionEvent?): Boolean {
        return true
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        doubleTapListener.invoke()
        return true
    }

}

class LongPressGestureListener(private val longPressListener: () -> Unit): GestureDetector.SimpleOnGestureListener() {

    override fun onDown(e: MotionEvent?): Boolean {
        return true
    }

    override fun onLongPress(e: MotionEvent?) {
        longPressListener.invoke()
    }
}