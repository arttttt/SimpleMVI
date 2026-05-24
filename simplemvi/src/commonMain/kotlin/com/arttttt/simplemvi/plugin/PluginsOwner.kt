package com.arttttt.simplemvi.plugin

/**
 * Exposes the list of [StorePlugin]s attached to a [com.arttttt.simplemvi.store.Store].
 *
 * Implemented by [com.arttttt.simplemvi.store.Store] so that callers can inspect or iterate the
 * plugin chain — useful for testing or diagnostics. The order of [plugins] is the order in which
 * the store invokes them.
 */
public interface PluginsOwner<Intent : Any, State : Any, SideEffect : Any> {

    public val plugins: List<StorePlugin<Intent, State, SideEffect>>
}
