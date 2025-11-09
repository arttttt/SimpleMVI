package com.arttttt.simplemvi.composition

import com.arttttt.simplemvi.plugin.StorePlugin
import com.arttttt.simplemvi.store.Store

/**
 * Composite plugin for managing multiple child stores and actors
 *
 * Delegates all lifecycle events to child wrappers (Store or Actor)
 */
public class ScopePlugin<ParentIntent : Any, ParentState : Any, ParentSideEffect : Any>(
    private val children: List<ScopedWrapper<ParentIntent, ParentState, ParentSideEffect>>,
) : StorePlugin<ParentIntent, ParentState, ParentSideEffect> {

    /**
     * Map of child stores by key for internal access
     */
    private val childStores: Map<String, Store<*, *, *>> by lazy {
        children.associate { it.key to it.getStore() }
    }

    /**
     * Gets child store by key with type casting
     *
     * @param key Unique child identifier
     * @return Child [Store]
     * @throws IllegalStateException if key not found
     */
    @Suppress("UNCHECKED_CAST")
    @PublishedApi
    internal fun <ChildIntent : Any, ChildState : Any, ChildSideEffect : Any> childStore(
        key: String,
    ): Store<ChildIntent, ChildState, ChildSideEffect> {
        val store = childStores[key] as? Store<ChildIntent, ChildState, ChildSideEffect>

        store ?: error("Child store with key '$key' not found. Available keys: ${childStores.keys}")

        return store
    }

    /**
     * Initializes parent state with values from all children
     */
    override fun provideInitialState(defaultState: ParentState): ParentState {
        return children.fold(defaultState) { state, child ->
            child.provideInitialState(state)
        }
    }

    /**
     * Starts all children
     */
    override fun onInit(context: StorePlugin.Context<ParentIntent, ParentState, ParentSideEffect>) {
        children.forEach { it.onInit(context) }
    }

    /**
     * Proxies intent to all children
     */
    override fun onIntent(intent: ParentIntent) {
        children.forEach { it.onIntent(intent) }
    }

    /**
     * Notifies all children about state change
     */
    override fun onStateChanged(oldState: ParentState, newState: ParentState) {
        children.forEach { it.onStateChanged(oldState, newState) }
    }

    /**
     * Notifies all children about side effect
     */
    override fun onSideEffect(sideEffect: ParentSideEffect) {
        children.forEach { it.onSideEffect(sideEffect) }
    }

    /**
     * Destroys all children
     */
    override fun onDestroy() {
        children.forEach { it.onDestroy() }
    }
}