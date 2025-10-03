package com.arttttt.simplemvi.store

import kotlin.jvm.JvmInline

/**
 * Value class representing the name of a [Store]
 *
 * This inline value class wraps a string name with no runtime overhead.
 * Used for store identification in logging and debugging.
 *
 * Example:
 * ```
 * val name = StoreName("MyStore")
 * ```
 *
 * Typically created using the [storeName] helper function:
 * ```
 * val name = storeName<MyStore>()
 * ```
 *
 * @param name The string name of the store
 */
@JvmInline
public value class StoreName(
    public val name: String,
)