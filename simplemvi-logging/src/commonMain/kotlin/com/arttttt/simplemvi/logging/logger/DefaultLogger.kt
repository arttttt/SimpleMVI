package com.arttttt.simplemvi.logging.logger

public object DefaultLogger : Logger {

    private const val TAG = "SimpleMVI"

    override fun log(message: String) {
        logV(TAG, message)
    }
}