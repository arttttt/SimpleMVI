package com.arttttt.simplemvi.config

import com.arttttt.simplemvi.logging.logger.Logger
import com.arttttt.simplemvi.logging.logger.DefaultLogger

/**
 * Global configuration for SimpleMVI
 */
internal var simpleMVIConfig: SimpleMVIConfig = DefaultSimpleMVIConfig()
    private set

/**
 * DSL marker annotation for SimpleMVI configuration
 *
 * This annotation ensures proper scoping of the configuration DSL
 * and prevents nested configuration blocks.
 */
@DslMarker
public annotation class SimpleMVIConfigDsl

/**
 * Configures SimpleMVI global behavior
 *
 * This function provides a DSL for configuring library-wide settings.
 * All stores created after calling this function will use the new configuration.
 *
 * Example:
 * ```
 * configureSimpleMVI {
 *     strictMode = true  // Enable strict mode for development
 *     logger = CustomLogger()  // Use custom logger
 * }
 * ```
 *
 * Common patterns:
 * ```
 * // Development configuration
 * configureSimpleMVI {
 *     strictMode = true
 *     logger = DebugLogger()
 * }
 *
 * // Production configuration
 * configureSimpleMVI {
 *     strictMode = false
 *     logger = AnalyticsLogger()
 * }
 *
 * // Disable all logging
 * configureSimpleMVI {
 *     logger = null
 * }
 * ```
 *
 * @param block Configuration lambda executed in the context of [SimpleMVIConfigBuilder]
 *
 * @see SimpleMVIConfig
 * @see SimpleMVIConfigBuilder
 */
@SimpleMVIConfigDsl
public fun configureSimpleMVI(block: SimpleMVIConfigBuilder.() -> Unit) {
    val builder = SimpleMVIConfigBuilder()
    builder.apply(block)
    simpleMVIConfig = builder.build()
}

/**
 * Builder class for SimpleMVI configuration
 *
 * This builder is used internally by [configureSimpleMVI] to construct
 * the configuration instance.
 *
 * @see configureSimpleMVI
 * @see SimpleMVIConfig
 */
@SimpleMVIConfigDsl
public class SimpleMVIConfigBuilder {
    /**
     * Defines the error handling mode for the library.
     *
     * When true (strict mode) - errors will throw exceptions.
     * When false (lenient mode) - errors will only be logged without interrupting execution.
     *
     * Default is true (strict mode).
     */
    public var strictMode: Boolean = false

    /**
     * Logger instance to be used by the library.
     * Default is DefaultLogger.
     * Set to null to disable logging.
     */
    public var logger: Logger? = DefaultLogger

    /**
     * Builds the [SimpleMVIConfig] instance from the current builder state
     *
     * @return Configured [SimpleMVIConfig] instance
     */
    internal fun build(): SimpleMVIConfig {
        return DefaultSimpleMVIConfig(
            strictMode = strictMode,
            logger = logger
        )
    }
}