package com.arttttt.simplemvi.config

import com.arttttt.simplemvi.logging.logger.Logger
import com.arttttt.simplemvi.logging.logger.DefaultLogger

/**
 * Global configuration for SimpleMVI
 */
internal var simpleMVIConfig: SimpleMVIConfig = DefaultSimpleMVIConfig()
    private set

/**
 * DSL marker for SimpleMVI configuration
 */
@DslMarker
public annotation class SimpleMVIConfigDsl

/**
 * Configures SimpleMVI behavior
 */
@SimpleMVIConfigDsl
public fun configureSimpleMVI(block: SimpleMVIConfigBuilder.() -> Unit) {
    val builder = SimpleMVIConfigBuilder()
    builder.apply(block)
    simpleMVIConfig = builder.build()
}

/**
 * Builder for SimpleMVI configuration
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

    internal fun build(): SimpleMVIConfig {
        return DefaultSimpleMVIConfig(
            strictMode = strictMode,
            logger = logger
        )
    }
}