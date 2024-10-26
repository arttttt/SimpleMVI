package com.arttttt.simplemvi.logging.logger

import android.util.Log

public actual fun logV(tag: String, message: String) {
    Log.v(tag, message)
}