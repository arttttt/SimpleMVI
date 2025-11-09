package com.arttttt.simplemvi.composition

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.plugin.StorePlugin
import com.arttttt.simplemvi.store.Store
import com.arttttt.simplemvi.store.createStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Internal wrapper for a single child actor
 */
internal class ScopedActorWrapper<
        ParentIntent : Any,
        ParentState : Any,
        ParentSideEffect : Any,
        Intent : Any,
        State : Any,
        SideEffect : Any,
        >(
    override val key: String,
    private val actor: Actor<Intent, State, SideEffect>,
    private val initialState: State,
    private val stateMerger: ParentState.(State) -> ParentState,
    private val intentUnwrapper: (ParentIntent) -> Intent?,
    private val intentWrapper: (Intent) -> ParentIntent,
    private val sideEffectWrapper: (SideEffect) -> ParentSideEffect?,
) : ScopedWrapper<ParentIntent, ParentState, ParentSideEffect> {

    private lateinit var context: StorePlugin.Context<ParentIntent, ParentState, ParentSideEffect>
    private lateinit var store: Store<Intent, State, SideEffect>
    private var stateJob: Job? = null
    private var sideEffectJob: Job? = null

    /**
     * Returns [Store] for external usage
     */
    override fun getStore(): Store<Intent, State, SideEffect> {
        return store
    }

    /**
     * Initializes parent state with child initial state
     */
    override fun provideInitialState(defaultState: ParentState): ParentState {
        return defaultState.stateMerger(initialState)
    }

    /**
     * Starts child store with ActorScope wrapper and subscribes to state/side effects
     */
    override fun onInit(context: StorePlugin.Context<ParentIntent, ParentState, ParentSideEffect>) {
        this.context = context

        // Create child store with actor
        // Actor will receive wrapped ActorScope that intercepts sendIntent
        store = createStore(
            name = null,
            initialize = false,
            initialState = initialState,
            actor = object : Actor<Intent, State, SideEffect> {

                override fun init(
                    scope: CoroutineScope,
                    getState: () -> State,
                    reduce: (State.() -> State) -> Unit,
                    onNewIntent: (Intent) -> Unit,
                    postSideEffect: (sideEffect: SideEffect) -> Unit
                ) {
                    actor.init(
                        scope = scope,
                        getState = getState,
                        reduce = reduce,
                        onNewIntent = { intent ->
                            context.sendIntent(intentWrapper(intent))
                        },
                        postSideEffect = postSideEffect,
                    )
                }

                override fun onIntent(intent: Intent) {
                    actor.onIntent(intent)
                }

                override fun destroy() {
                    actor.destroy()
                }
            },
        )

        // Initialize child store
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
    override fun onIntent(intent: ParentIntent) {
        intentUnwrapper(intent)?.let(store::accept)
    }

    /**
     * Notification about state change (can be used for additional logic)
     */
    override fun onStateChanged(oldState: ParentState, newState: ParentState) {
        // Not used
    }

    /**
     * Notification about side effect (can be used for additional logic)
     */
    override fun onSideEffect(sideEffect: ParentSideEffect) {
        // Not used
    }

    /**
     * Destroys child and cancels subscriptions
     */
    override fun onDestroy() {
        stateJob?.cancel()
        sideEffectJob?.cancel()
        store.destroy()
    }
}