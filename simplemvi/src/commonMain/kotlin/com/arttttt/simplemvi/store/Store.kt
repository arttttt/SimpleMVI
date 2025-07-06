package com.arttttt.simplemvi.store

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.middleware.Middleware
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
 * [Store] also supports [Middleware]. [Middleware] can be used as a spy. It receives all store events,
 * but you can not modify them
 *
 * @see Actor
 * @see Middleware
 */
public interface Store<in Intent : Any, out State : Any, out SideEffect : Any> {

    /**
     * Returns [Store] state
     */
    public val state: State

    /**
     * Returns [Store] states [Flow]
     *
     * When a new [State] is emitted, it's available inside the [Middleware]
     */
    public val states: StateFlow<State>

    /**
     * Returns [Store] side effects [Flow]
     *
     * When [SideEffect] is emitted, it's available inside the [Middleware]
     */
    public val sideEffects: Flow<SideEffect>

    /**
     * This function initializes [Store]
     */
    public fun init()

    /**
     * This function accepts an [Intent] and passes it to the [Actor]
     *
     * [Intent] is also available inside the [Middleware]
     */
    public fun accept(intent: Intent)

    /**
     * This function destroyes the [Store]
     * [Store] can not be used after it was destroyed
     */
    public fun destroy()
}