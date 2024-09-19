package com.arttttt.simplemvi.utils

@MainThread
public fun assertOnMainThread() {
    require(isMainThread()) {
        "This call must be made on the main thread"
    }
}

public expect fun isMainThread(): Boolean