package com.arttttt.simplemvi.utils.mainthread

import android.os.Looper

actual fun isMainThread(): Boolean {
    return Looper.getMainLooper() == Looper.myLooper()
}