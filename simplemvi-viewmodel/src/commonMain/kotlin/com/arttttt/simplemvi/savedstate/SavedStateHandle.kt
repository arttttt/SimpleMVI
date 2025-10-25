package com.arttttt.simplemvi.savedstate

/**
 * Cross-platform saved state container.
 */
public expect class SavedStateHandle {

    /**
     * Gets saved value.
     */
    public fun get(key: String): String?

    /**
     * Sets value to save.
     */
    public fun set(key: String, value: String)
}