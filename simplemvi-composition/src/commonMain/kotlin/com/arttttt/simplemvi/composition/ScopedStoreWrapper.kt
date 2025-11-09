package com.arttttt.simplemvi.composition

import com.arttttt.simplemvi.plugin.StorePlugin
import com.arttttt.simplemvi.store.Store
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Internal wrapper for a single child store
 */
public class ScopedStoreWrapper<
        ParentIntent : Any,
        ParentState : Any,
        ParentSideEffect : Any,
        Intent : Any,
        State : Any,
        SideEffect : Any,
        >(
    override val key: String,
    private val store: Store<Intent, State, SideEffect>,
    private val stateMerger: ParentState.(State) -> ParentState,
    private val intentUnwrapper: (ParentIntent) -> Intent?,
    private val sideEffectWrapper: (SideEffect) -> ParentSideEffect?,
) : ScopedWrapper<ParentIntent, ParentState, ParentSideEffect> {

    private lateinit var context: StorePlugin.Context<ParentIntent, ParentState, ParentSideEffect>
    private var stateJob: Job? = null
    private var sideEffectJob: Job? = null

    override fun getStore(): Store<*, *, *> {
        return store
    }

    /**
     * Initializes parent state with child state value
     */
    public override fun provideInitialState(defaultState: ParentState): ParentState {
        return defaultState.stateMerger(store.state)
    }

    /**
     * Starts child and subscribes to its state and side effects
     */
    public override fun onInit(context: StorePlugin.Context<ParentIntent, ParentState, ParentSideEffect>) {
        this.context = context

        // Initialize child
        store.init()

        // Sync state: child → parent
        stateJob = store
            .states
            .onEach { state ->
                context.setState(
                    context.state.stateMerger(state)
                )
            }
            .launchIn(context.scope)

        // Translate side effects: child → parent (wrapped)
        sideEffectJob = store
            .sideEffects
            .onEach { sideEffect ->
                sideEffectWrapper
                    .invoke(sideEffect)
                    ?.let(context.sendSideEffect)
            }
            .launchIn(context.scope)
    }

    /**
     * Proxies wrapped parent intent to child
     */
    public override fun onIntent(intent: ParentIntent) {
        intentUnwrapper(intent)?.let(store::accept)
    }

    /**
     * Notification about state change
     */
    public override fun onStateChanged(oldState: ParentState, newState: ParentState) {
        // Not used
    }

    /**
     * Notification about side effect
     */
    public override fun onSideEffect(sideEffect: ParentSideEffect) {
        // Not used
    }

    /**
     * Destroys child and cancels subscriptions
     */
    public override fun onDestroy() {
        stateJob?.cancel()
        sideEffectJob?.cancel()
        store.destroy()
    }
}