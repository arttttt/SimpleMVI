package com.arttttt.simplemvi.utils.mainthread

@MainThread
fun assertOnMainThread() {
    require(isMainThread()) {
        "This call must be made on the main thread"
    }
}

expect fun isMainThread(): Boolean