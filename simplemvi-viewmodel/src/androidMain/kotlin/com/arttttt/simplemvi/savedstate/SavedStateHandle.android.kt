package com.arttttt.simplemvi.savedstate

import android.os.Bundle
import androidx.savedstate.SavedState

/**
 * Android implementation using SavedStateRegistry.
 */
public actual class SavedStateHandle(
    handle: androidx.lifecycle.SavedStateHandle,
) {

    private companion object {
        private const val KEY = "SimpleMVI_SavedState"
    }

    private val container: SavedState by lazy {
        handle[KEY] ?: SavedState()
    }

    init {
        handle.setSavedStateProvider(KEY) {
            container
        }
    }

    public actual fun get(key: String): String? {
        return container.getString(key)
    }

    public actual fun set(key: String, value: String) {
        container.putString(key, value)
    }
}

public fun androidx.lifecycle.SavedStateHandle.toPluginSavedStateHandle(): SavedStateHandle {
    return SavedStateHandle(this)
}