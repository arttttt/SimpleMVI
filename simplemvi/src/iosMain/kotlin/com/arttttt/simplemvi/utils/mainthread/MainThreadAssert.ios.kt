package com.arttttt.simplemvi.utils.mainthread

import platform.Foundation.NSThread

actual fun isMainThread(): Boolean {
    return NSThread.isMainThread
}