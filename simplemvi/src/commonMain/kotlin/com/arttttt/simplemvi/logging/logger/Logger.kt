package com.arttttt.simplemvi.logging.logger

/**
 * Logger interface for SimpleMVI logging functionality
 *
 * Implementations of this interface define how log messages are output.
 * The library provides [DefaultLogger] which uses platform-specific
 * logging mechanisms.
 *
 * Custom implementations can forward logs to any logging system:
 * - Timber (Android)
 * - os_log (iOS)
 * - Custom analytics systems
 * - File logging
 *
 * Example custom logger:
 * ```
 * val customLogger = Logger { message ->
 *     Timber.d(message)
 * }
 *
 * configureSimpleMVI {
 *     logger = customLogger
 * }
 * ```
 */
public fun interface Logger {

    /**
     * Logs a message
     *
     * @param message The message to log
     */
    public fun log(message: String)
}