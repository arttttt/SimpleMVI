package com.arttttt.simplemvi.config

import com.arttttt.simplemvi.logging.logger.Logger
import com.arttttt.simplemvi.logging.logger.DefaultLogger

/**
 * Configuration for SimpleMVI behavior
 */
public interface SimpleMVIConfig {
    /**
     * Defines the error handling mode for the library.
     *
     * When true (strict mode) - errors will throw exceptions.
     * When false (lenient mode) - errors will only be logged without interrupting execution.
     *
     * It's recommended to use true for development and false for production.
     */
    public val strictMode: Boolean

    /**
     * Logger instance used by the library.
     * When null, logging is disabled.
     */
    public val logger: Logger?
}

/**
 * Default implementation of the configuration
 */
public data class DefaultSimpleMVIConfig(
    override val strictMode: Boolean = false,
    override val logger: Logger? = DefaultLogger
) : SimpleMVIConfig