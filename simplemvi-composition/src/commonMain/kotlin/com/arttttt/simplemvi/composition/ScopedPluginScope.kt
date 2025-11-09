package com.arttttt.simplemvi.composition

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.plugin.StorePlugin
import com.arttttt.simplemvi.store.Store

/**
 * Creates a type-safe key for child store identification
 */
public inline fun <reified T : Any> key(): String {
    return T::class.qualifiedName
        ?: T::class.simpleName
        ?: error("Cannot create key for anonymous class ${T::class}")
}

/**
 * Scope for registering child stores and actors
 */
public class ScopePluginScope<ParentIntent : Any, ParentState : Any, ParentSideEffect : Any> {

    private val children = mutableListOf<ScopedWrapper<ParentIntent, ParentState, ParentSideEffect>>()

    /**
     * Registers a child store
     *
     * @param key Unique child identifier for external access
     * @param store Child store
     * @param updateParentState Updates parent state with new child state
     * @param unwrapIntent Extracts child intent from parent intent (null if not a wrapper)
     * @param wrapSideEffect Wraps child side effect into parent side effect
     */
    public fun <ChildIntent : Any, ChildState : Any, ChildSideEffect : Any> scope(
        key: String,
        store: Store<ChildIntent, ChildState, ChildSideEffect>,
        updateParentState: ParentState.(ChildState) -> ParentState,
        unwrapIntent: (ParentIntent) -> ChildIntent?,
        wrapSideEffect: (ChildSideEffect) -> ParentSideEffect?,
    ) {
        children += ScopedStoreWrapper(
            key = key,
            store = store,
            stateMerger = updateParentState,
            intentUnwrapper = unwrapIntent,
            sideEffectWrapper = wrapSideEffect,
        )
    }

    /**
     * Registers a child actor with ActorScope interception
     *
     * @param key Unique child identifier
     * @param actor Actor instance
     * @param initialState Initial state for child actor
     * @param updateParentState Updates parent state with new child state
     * @param unwrapIntent Extracts child intent from parent intent (null if not a wrapper)
     * @param wrapIntent Wraps child intent into parent intent for logging/plugins
     * @param wrapSideEffect Wraps child side effect into parent side effect
     */
    public fun <ChildIntent : Any, ChildState : Any, ChildSideEffect : Any> scope(
        key: String,
        actor: Actor<ChildIntent, ChildState, ChildSideEffect>,
        initialState: ChildState,
        updateParentState: ParentState.(ChildState) -> ParentState,
        unwrapIntent: (ParentIntent) -> ChildIntent?,
        wrapIntent: (ChildIntent) -> ParentIntent,
        wrapSideEffect: (ChildSideEffect) -> ParentSideEffect?,
    ) {
        children += ScopedActorWrapper(
            key = key,
            actor = actor,
            initialState = initialState,
            stateMerger = updateParentState,
            intentUnwrapper = unwrapIntent,
            intentWrapper = wrapIntent,
            sideEffectWrapper = wrapSideEffect,
        )
    }

    internal fun build(): ScopePlugin<ParentIntent, ParentState, ParentSideEffect> {
        return ScopePlugin(children.toList())
    }
}

/**
 * DSL for creating a composite scope plugin
 */
public fun <ParentIntent : Any, ParentState : Any, ParentSideEffect : Any> scopePlugin(
    block: ScopePluginScope<ParentIntent, ParentState, ParentSideEffect>.() -> Unit,
): StorePlugin<ParentIntent, ParentState, ParentSideEffect> {
    return ScopePluginScope<ParentIntent, ParentState, ParentSideEffect>()
        .apply(block)
        .build()
}