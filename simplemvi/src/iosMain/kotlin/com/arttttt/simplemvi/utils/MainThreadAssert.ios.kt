package com.arttttt.simplemvi.utils

import platform.Foundation.NSThread

actual fun isMainThread(): Boolean {
    return NSThread.isMainThread
}