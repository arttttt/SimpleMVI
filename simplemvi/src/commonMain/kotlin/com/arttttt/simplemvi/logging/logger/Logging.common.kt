package com.arttttt.simplemvi.logging.logger

/**
 * Platform-specific logging function
 *
 * Each supported platform must provide an implementation of this function.
 * The actual implementation is defined in platform-specific source sets.
 *
 * @param tag The log tag/category
 * @param message The message to log
 *
 * @see DefaultLogger
 */
public expect fun logV(tag: String, message: String)