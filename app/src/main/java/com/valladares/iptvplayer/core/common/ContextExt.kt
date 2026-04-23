package com.valladares.iptvplayer.core.common

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Walks wrapped contexts and returns the nearest [Activity], if present.
 */
fun Context.findActivity(): Activity? {
    var current: Context = this
    while (current is ContextWrapper) {
        if (current is Activity) {
            return current
        }
        current = current.baseContext
    }
    return null
}
