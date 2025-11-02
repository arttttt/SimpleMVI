package com.arttttt.simplemvi.composition

import com.arttttt.simplemvi.plugin.StorePlugin

/**
 * Composite plugin for managing multiple child stores
 *
 * Delegates all lifecycle events to child store wrappers
 */
public class ChildStorePlugin<ParentIntent : Any, ParentState : Any, ParentSideEffect : Any>(
    private val children: List<ChildStoreWrapper<ParentIntent, ParentState, ParentSideEffect, *, *, *>>,
) : StorePlugin<ParentIntent, ParentState, ParentSideEffect> {

    /**
     * Initializes parent state with values from all child stores
     */
    override fun provideInitialState(defaultState: ParentState): ParentState {
        return children.fold(defaultState) { state, child ->
            child.provideInitialState(state)
        }
    }

    /**
     * Starts all child stores
     */
    override fun onInit(context: StorePlugin.Context<ParentIntent, ParentState, ParentSideEffect>) {
        children.forEach { it.onInit(context) }
    }

    /**
     * Proxies intent to all child stores
     */
    override fun onIntent(intent: ParentIntent) {
        children.forEach { it.onIntent(intent) }
    }

    /**
     * Notifies all child stores about state change
     */
    override fun onStateChanged(oldState: ParentState, newState: ParentState) {
        children.forEach { it.onStateChanged(oldState, newState) }
    }

    /**
     * Notifies all child stores about side effect
     */
    override fun onSideEffect(sideEffect: ParentSideEffect) {
        children.forEach { it.onSideEffect(sideEffect) }
    }

    /**
     * Destroys all child stores
     */
    override fun onDestroy() {
        children.forEach { it.onDestroy() }
    }
}