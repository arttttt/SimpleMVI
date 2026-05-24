package com.arttttt.simplemvi.plugin

import kotlinx.coroutines.CoroutineScope

/**
 * A [StorePlugin] extends a [com.arttttt.simplemvi.store.Store] without modifying its [com.arttttt.simplemvi.actor.Actor].
 *
 * Plugins observe and participate in every stage of the [com.arttttt.simplemvi.store.Store] lifecycle:
 * - initial state composition (see [provideInitialState])
 * - initialization (see [onInit])
 * - intent processing (see [onIntent] — can [Pipeline.block] or [Pipeline.transform] intents)
 * - state changes (see [onStateChanged])
 * - side effects (see [onSideEffect])
 * - destruction (see [onDestroy])
 *
 * Multiple plugins can be registered on a single [com.arttttt.simplemvi.store.Store]. They are invoked
 * in the order they were passed to `createStore`. For [onIntent] this order is also the order in
 * which the [Pipeline] runs — earlier plugins can rewrite or block intents before later plugins see them.
 *
 * @see Pipeline
 * @see com.arttttt.simplemvi.store.createStore
 */
public interface StorePlugin<Intent : Any, State : Any, SideEffect : Any> {

    /**
     * Runtime handles given to a [StorePlugin] in [onInit].
     *
     * A plugin can use [Context] to inject intents, push state directly, emit side effects,
     * launch coroutines tied to the store lifecycle, and read the current state at any time.
     *
     * @property scope coroutine scope tied to the store lifecycle. Cancelled when the store is destroyed.
     * @property sendIntent dispatches an [Intent] to the store (equivalent to `store.accept(intent)`).
     * @property setState replaces the current state. Triggers [onStateChanged] on every plugin.
     * @property sendSideEffect emits a [SideEffect] through the store.
     * @property state current store state. Always returns the latest value.
     */
    public data class Context<Intent : Any, State : Any, SideEffect : Any>(
        val scope: CoroutineScope,
        val sendIntent: (Intent) -> Unit,
        val setState: (State) -> Unit,
        val sendSideEffect: (SideEffect) -> Unit,
        private val getState: () -> State,
    ) {

        val state: State
            get() = getState()
    }

    /**
     * Allows the plugin to transform the initial state before the store starts.
     *
     * Plugins are folded over the initial state in registration order — each plugin receives
     * the result of the previous one. Override this to hydrate state from persistence, apply
     * defaults, or layer extra fields on top of the default state.
     *
     * @param defaultState the state produced so far (the original initial state for the first plugin,
     *   the previous plugin's output for the rest).
     * @return the state this plugin contributes to the chain. Default implementation returns [defaultState] unchanged.
     */
    public fun provideInitialState(defaultState: State): State = defaultState

    /**
     * Called once when the store is initialized, before any intents are processed.
     *
     * Use this to cache the [Context], subscribe to external sources, or run startup work.
     */
    public fun onInit(context: Context<Intent, State, SideEffect>) {}

    /**
     * Called when the store receives an [Intent], before the [com.arttttt.simplemvi.actor.Actor] sees it.
     *
     * Receiver is a [Pipeline] — call [Pipeline.transform] to rewrite the intent for downstream
     * plugins and the actor, or [Pipeline.block] to drop it entirely. If neither is called, the
     * intent passes through unchanged.
     */
    public fun Pipeline<Intent>.onIntent(intent: Intent) {}

    /**
     * Called after every state change, with both the previous and the new value.
     *
     * Fires for changes produced by the actor's `reduce` blocks and by [Context.setState] calls
     * from any plugin.
     */
    public fun onStateChanged(oldState: State, newState: State) {}

    /**
     * Called when the store emits a [SideEffect], before it reaches subscribers of
     * [com.arttttt.simplemvi.store.Store.sideEffects].
     */
    public fun onSideEffect(sideEffect: SideEffect) {}

    /**
     * Called when the store is being destroyed. The store's [CoroutineScope] is still active here —
     * use it to flush buffered work. After this returns, the scope is cancelled.
     */
    public fun onDestroy() {}
}
