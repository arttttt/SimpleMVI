package com.arttttt.simplemvi.composition

import com.arttttt.simplemvi.plugin.StorePlugin
import com.arttttt.simplemvi.store.Store
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Internal wrapper for a single child store
 */
public class ChildStoreWrapper<
        ParentIntent : Any,
        ParentState : Any,
        ParentSideEffect : Any,
        ChildIntent : Any,
        ChildState : Any,
        ChildSideEffect : Any,
        >(
    private val child: Store<ChildIntent, ChildState, ChildSideEffect>,
    private val stateMerger: ParentState.(ChildState) -> ParentState,
    private val intentUnwrapper: (ParentIntent) -> ChildIntent?,
    private val sideEffectWrapper: (ChildSideEffect) -> ParentSideEffect?,
) {

    private lateinit var context: StorePlugin.Context<ParentIntent, ParentState, ParentSideEffect>
    private var stateJob: Job? = null
    private var sideEffectJob: Job? = null

    /**
     * Initializes parent state with child state value
     */
    public fun provideInitialState(defaultState: ParentState): ParentState {
        return defaultState.stateMerger(child.state)
    }

    /**
     * Starts child and subscribes to its state and side effects
     */
    public fun onInit(context: StorePlugin.Context<ParentIntent, ParentState, ParentSideEffect>) {
        this.context = context

        // Initialize child
        child.init()

        // Sync state: child → parent
        stateJob = context.scope.launch {
            child.states.collect { childState ->
                context.setState(
                    context.state.stateMerger(childState)
                )
            }
        }

        // Translate side effects: child → parent (wrapped)
        sideEffectJob = context.scope.launch {
            child.sideEffects.collect { childEffect ->
                sideEffectWrapper
                    .invoke(childEffect)
                    ?.let(context.sendSideEffect)
            }
        }
    }

    /**
     * Proxies wrapped parent intent to child
     */
    public fun onIntent(intent: ParentIntent) {
        intentUnwrapper(intent)?.let { childIntent ->
            child.accept(childIntent)
        }
    }

    /**
     * Notification about state change (can be used for additional logic)
     */
    public fun onStateChanged(oldState: ParentState, newState: ParentState) {
        // Not used yet, but may be useful for custom logic
    }

    /**
     * Notification about side effect (can be used for additional logic)
     */
    public fun onSideEffect(sideEffect: ParentSideEffect) {
        // Not used yet, but may be useful for custom logic
    }

    /**
     * Destroys child and cancels subscriptions
     */
    public fun onDestroy() {
        stateJob?.cancel()
        sideEffectJob?.cancel()
        child.destroy()
    }
}