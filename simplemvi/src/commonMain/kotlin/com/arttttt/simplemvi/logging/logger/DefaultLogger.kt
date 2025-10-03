package com.arttttt.simplemvi.logging.logger

import com.arttttt.simplemvi.config.configureSimpleMVI

/**
 * Default [Logger] implementation using platform-specific logging
 *
 * All log messages are tagged with "SimpleMVI".
 *
 * This logger is used by default unless a custom logger is configured
 * via [configureSimpleMVI].
 *
 * @see Logger
 */
public object DefaultLogger : Logger {

    private const val TAG = "SimpleMVI"

    override fun log(message: String) {
        logV(TAG, message)
    }
}