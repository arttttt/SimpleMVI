package com.arttttt.simplemvi.composition

import com.arttttt.simplemvi.plugin.PluginsOwner
import com.arttttt.simplemvi.store.Store

/**
 * Gets a scoped child store from parent store's ScopePlugin
 *
 * @param key Unique child identifier (use key<ChildState>())
 * @return Child store
 * @throws IllegalStateException if ScopePlugin not found or key not found
 */
public inline fun <reified Intent : Any, reified State : Any, reified SideEffect : Any> PluginsOwner<*, *, *>.scoped(
    key: String = key<State>(),
): Store<Intent, State, SideEffect> {
    val scopePlugin = plugins
        .filterIsInstance<ScopePlugin<*, *, *>>()
        .firstOrNull()
        ?: error("ScopePlugin not found in store's plugins. Make sure to add scopePlugin { } to the store's plugins list.")

    return scopePlugin.childStore(key)
}