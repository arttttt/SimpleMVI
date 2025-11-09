package com.arttttt.simplemvi.composition

import com.arttttt.simplemvi.plugin.PluginsOwner
import com.arttttt.simplemvi.store.Store

/**
 * Gets a scoped child store from parent store's ScopePlugin
 *
 * This is the public API for accessing child stores from a parent store.
 * Similar to TCA's `store.scope(state:action:)`
 *
 * Example:
 * ```kotlin
 * val feedStore: Store<FeedIntent, FeedState, FeedSideEffect> =
 *     parentStore.scoped(key<FeedState>())
 * ```
 *
 * @param key Unique child identifier (use key<ChildState>())
 * @return Child store
 * @throws IllegalStateException if ScopePlugin not found or key not found
 */
public inline fun <reified Intent : Any, reified State : Any, reified SideEffect : Any> Store<*, *, *>.scoped(
    key: String = key<State>(),
): Store<Intent, State, SideEffect> {
    require(this is PluginsOwner<*, *, *>) { "Store must be a PluginsOwner" }

    val scopePlugin = plugins
        .filterIsInstance<ScopePlugin<*, *, *>>()
        .firstOrNull()
        ?: error("ScopePlugin not found in store's plugins. Make sure to add scopePlugin { } to the store's plugins list.")

    return scopePlugin.childStore(key)
}