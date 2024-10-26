package com.arttttt.simplemvi.logging.logger

/**
 * A default [Logger] implementation
 *
 * @see Logger
 */
public object DefaultLogger : Logger {

    private const val TAG = "SimpleMVI"

    override fun log(message: String) {
        logV(TAG, message)
    }
}