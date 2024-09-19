package com.arttttt.simplemvi.utils

import platform.Foundation.NSThread

public actual fun isMainThread(): Boolean {
    return NSThread.isMainThread
}