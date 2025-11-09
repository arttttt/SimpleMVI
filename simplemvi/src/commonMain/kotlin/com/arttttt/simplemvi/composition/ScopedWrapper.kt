package com.arttttt.simplemvi.composition

import com.arttttt.simplemvi.plugin.StorePlugin
import com.arttttt.simplemvi.store.Store

/**
 * Common interface for child wrappers
 */
public interface ScopedWrapper<ParentIntent : Any, ParentState : Any, ParentSideEffect : Any> {

    /**
     * Unique child identifier for external access
     */
    public val key: String

    /**
     * Returns child Store for external usage
     */
    public fun getStore(): Store<*, *, *>

    /**
     * Initializes parent state with child state value
     */
    public fun provideInitialState(defaultState: ParentState): ParentState

    /**
     * Starts child and subscribes to its state and side effects
     */
    public fun onInit(context: StorePlugin.Context<ParentIntent, ParentState, ParentSideEffect>)

    /**
     * Proxies wrapped parent intent to child
     */
    public fun onIntent(intent: ParentIntent)

    /**
     * Notification about state change (can be used for additional logic)
     */
    public fun onStateChanged(oldState: ParentState, newState: ParentState)

    /**
     * Notification about side effect (can be used for additional logic)
     */
    public fun onSideEffect(sideEffect: ParentSideEffect)

    /**
     * Destroys child and cancels subscriptions
     */
    public fun onDestroy()
}