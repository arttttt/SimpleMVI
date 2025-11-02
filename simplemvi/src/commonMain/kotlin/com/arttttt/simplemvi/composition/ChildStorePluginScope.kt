package com.arttttt.simplemvi.composition

import com.arttttt.simplemvi.plugin.StorePlugin
import com.arttttt.simplemvi.store.Store

/**
 * Scope for registering child stores
 */
public class ChildStorePluginScope<ParentIntent : Any, ParentState : Any, ParentSideEffect : Any> {

    private val children = mutableListOf<ChildStoreWrapper<ParentIntent, ParentState, ParentSideEffect, *, *, *>>()

    /**
     * Registers a child store
     *
     * @param store Child store
     * @param updateParentState Updates parent state with new child state
     * @param unwrapIntent Extracts child intent from parent intent (null if not a wrapper)
     * @param wrapSideEffect Wraps child side effect into parent side effect
     */
    public fun <ChildIntent : Any, ChildState : Any, ChildSideEffect : Any> install(
        store: Store<ChildIntent, ChildState, ChildSideEffect>,
        updateParentState: ParentState.(ChildState) -> ParentState,
        unwrapIntent: (ParentIntent) -> ChildIntent?,
        wrapSideEffect: (ChildSideEffect) -> ParentSideEffect?,
    ) {
        children += ChildStoreWrapper(
            child = store,
            stateMerger = updateParentState,
            intentUnwrapper = unwrapIntent,
            sideEffectWrapper = wrapSideEffect,
        )
    }

    internal fun build(): ChildStorePlugin<ParentIntent, ParentState, ParentSideEffect> {
        return ChildStorePlugin(children.toList())
    }
}

/**
 * DSL for creating a composite child store plugin
 *
 * Example:
 * ```kotlin
 * plugins = listOf(
 *     childStorePlugin {
 *         install(
 *             store = feedStore,
 *             getChildState = { feed },
 *             updateParentState = { copy(feed = it) },
 *             unwrapIntent = { (it as? HomeIntent.Feed)?.intent },
 *             wrapSideEffect = { HomeSideEffect.Feed(it) },
 *         )
 *
 *         install(
 *             store = accountStore,
 *             getChildState = { account },
 *             updateParentState = { copy(account = it) },
 *             unwrapIntent = { (it as? HomeIntent.Account)?.intent },
 *             wrapSideEffect = { HomeSideEffect.Account(it) },
 *         )
 *     }
 * )
 * ```
 */
public fun <ParentIntent : Any, ParentState : Any, ParentSideEffect : Any> childStorePlugin(
    block: ChildStorePluginScope<ParentIntent, ParentState, ParentSideEffect>.() -> Unit,
): StorePlugin<ParentIntent, ParentState, ParentSideEffect> {
    return ChildStorePluginScope<ParentIntent, ParentState, ParentSideEffect>()
        .apply(block)
        .build()
}