package com.arttttt.simplemvi.utils

/**
 * this function checks that the method is called on the main thread
 */
@MainThread
public fun assertOnMainThread() {
    require(isMainThread()) {
        "This call must be made on the main thread"
    }
}

/**
 * each supported platform must provide a main thread check
 */
public expect fun isMainThread(): Boolean