package com.arttttt.simplemvi.store

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.plugin.StorePlugin
import com.arttttt.simplemvi.plugin.PluginsOwner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * [Store] is made to encapsulate different logic
 * It allows you to design apps using unidirectional data flow and single source of truth
 *
 * [Store] has its own [State], it also can accept [Intent] and emit [SideEffect]
 *
 * [Store] must be initialized before using by calling [Store.init] and
 * [Store.destroy] must be called when you don't need [Store] anymore for freeing up resources
 *
 * Every [Store] must have an [Actor]. [Actor] contains all [Store] logic.
 * [Actor] handles [Intent] which you pass to a [Store]
 *
 * [Store] also supports [StorePlugin]. Plugins extend the [Store] by observing its lifecycle
 * (init, state changes, side effects, destroy) and by participating in intent processing through
 * the [com.arttttt.simplemvi.plugin.Pipeline] — a plugin can rewrite or drop an [Intent] before
 * it reaches the [Actor]. The list of attached plugins is exposed via [plugins].
 *
 * @see Actor
 * @see StorePlugin
 * @see com.arttttt.simplemvi.plugin.Pipeline
 */
public interface Store<Intent : Any, State : Any, SideEffect : Any> : PluginsOwner<Intent, State, SideEffect> {

    /**
     * Returns [Store] state
     */
    public val state: State

    /**
     * Returns [Store] states [Flow]
     *
     * When a new [State] is emitted, it's available inside the [StorePlugin]
     */
    public val states: StateFlow<State>

    /**
     * Returns [Store] side effects [Flow]
     *
     * When [SideEffect] is emitted, it's available inside the [StorePlugin]
     */
    public val sideEffects: Flow<SideEffect>

    /**
     * This function initializes [Store]
     */
    public fun init()

    /**
     * This function accepts an [Intent] and passes it to the [Actor]
     *
     * [Intent] is also available inside the [StorePlugin]
     */
    public fun accept(intent: Intent)

    /**
     * This function destroys the [Store]
     * [Store] can not be used after it was destroyed
     */
    public fun destroy()
}