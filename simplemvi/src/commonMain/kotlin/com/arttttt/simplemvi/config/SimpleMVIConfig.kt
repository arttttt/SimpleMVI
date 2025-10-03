package com.arttttt.simplemvi.config

import com.arttttt.simplemvi.logging.logger.DefaultLogger
import com.arttttt.simplemvi.logging.logger.Logger

/**
 * Configuration interface for SimpleMVI behavior
 *
 * This interface defines the global configuration options for the library.
 * Use [configureSimpleMVI] to set these options.
 *
 * @see configureSimpleMVI
 * @see DefaultSimpleMVIConfig
 */
public interface SimpleMVIConfig {

    /**
     * Defines the error handling mode for the library
     *
     * Error handling modes:
     * - `true` (strict mode): Errors throw exceptions that crash the app.
     *   Recommended for development to catch issues early.
     * - `false` (lenient mode): Errors are only logged without interrupting execution.
     *   Recommended for production to prevent crashes from store misuse.
     */
    public val strictMode: Boolean

    /**
     * Logger instance used by the library
     *
     * @see Logger
     * @see DefaultLogger
     */
    public val logger: Logger?
}

/**
 * Default implementation of [SimpleMVIConfig]
 *
 * This data class provides the default configuration values:
 * - `strictMode = false` (lenient mode)
 * - `logger = DefaultLogger`
 *
 * @param strictMode Error handling mode. Default is false (lenient)
 * @param logger Logger instance. Default is [DefaultLogger]
 *
 * @see SimpleMVIConfig
 * @see configureSimpleMVI
 */
public data class DefaultSimpleMVIConfig(
    override val strictMode: Boolean = false,
    override val logger: Logger? = DefaultLogger
) : SimpleMVIConfig