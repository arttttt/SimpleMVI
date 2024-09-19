package com.arttttt.simplemvi.utils

import android.os.Looper

public actual fun isMainThread(): Boolean {
    return Looper.getMainLooper() == Looper.myLooper()
}