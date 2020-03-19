package com.cronus.zdone.util

object Do {
    inline infix fun <reified T> exhaustive(any: T?) = any
}