package com.arttttt.simplemvi.logging

import com.arttttt.simplemvi.config.configureSimpleMVI
import com.arttttt.simplemvi.config.simpleMVIConfig
import com.arttttt.simplemvi.logging.logger.Logger

/**
 * @deprecated Use SimpleMVI configuration to set logger instead.
 * This function is kept for backward compatibility and will be removed in future versions.
 *
 * Example:
 * ```
 * configureSimpleMVI {
 *     logger = myCustomLogger
 * }
 * ```
 */
@Deprecated(
    message = "Use SimpleMVI configuration to set logger instead",
    replaceWith = ReplaceWith("configureSimpleMVI { logger = logger }", "com.arttttt.simplemvi.config.configureSimpleMVI")
)
public fun setDefaultLogger(logger: Logger?) {
    // Use the DSL to update only the logger while preserving other settings
    configureSimpleMVI {
        // Set the new logger
        this.logger = logger
        // Preserve the current strictMode setting
        strictMode = simpleMVIConfig.strictMode
    }
}