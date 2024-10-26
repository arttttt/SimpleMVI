package com.arttttt.simplemvi.logging

import com.arttttt.simplemvi.logging.logger.DefaultLogger
import com.arttttt.simplemvi.logging.logger.Logger

/**
 * Global logger instance
 * Can be overwritten via [setDefaultLogger] function
 */
internal var defaultLogger: Logger? = DefaultLogger
    private set

public fun setDefaultLogger(logger: Logger?) {
    defaultLogger = logger
}