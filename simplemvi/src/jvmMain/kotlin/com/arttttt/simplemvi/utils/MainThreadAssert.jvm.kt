package com.arttttt.simplemvi.utils

import javax.swing.SwingUtilities

/**
 * Use this global variable to set a custom main thread check if you use a custom UI framework.
 */
@Volatile
public var isMainThreadResolver: () -> Boolean = {
    SwingUtilities.isEventDispatchThread()
}

/**
 * each supported platform must provide a main thread check
 */
public actual fun isMainThread(): Boolean = isMainThreadResolver()
