package com.arttttt.simplemvi.actor

import com.arttttt.simplemvi.store.Store
import com.arttttt.simplemvi.utils.MainThread

/**
 * a set of properties and functions to be used inside the [Actor]
 *
 * @see Actor
 */
public interface ActorScope<in Intent : Any, State : Any, in SideEffect : Any> {

    /**
     * Returns current [State] of the [Store]
     */
    public val state: State

    /**
     * Sometimes it's necessary to fire a new intent within an [Actor]
     * This can be done by calling this function
     *
     * @param intent [Intent] to be handled by the [Actor]
     */
    @MainThread
    public fun intent(intent: Intent)

    /**
     * This function is called when a new [State] needs to be produced
     *
     * @param block a lambda that receives the current [State] and returns a new [State]
     */
    @MainThread
    public fun reduce(block: State.() -> State)

    /**
     * This function is called when a new [SideEffect] needs to be emitted
     *
     * @param sideEffect [SideEffect] to be emitted
     */
    @MainThread
    public fun sideEffect(sideEffect: SideEffect)
}