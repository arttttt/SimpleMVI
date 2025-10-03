package com.arttttt.simplemvi.middleware

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.store.Store
import kotlinx.coroutines.CoroutineScope

/**
 * Middleware provides a way to observe and react to [Store] events without modifying them
 *
 * [Middleware] acts as a passive observer that receives notifications about all
 * events happening in the [Store]:
 * - Store initialization
 * - Intent reception
 * - State changes
 * - Side effect emission
 * - Store destruction
 *
 * Common use cases include:
 * - Logging store events for debugging
 * - Sending analytics events
 * - Performance monitoring
 *
 * Important: [Middleware] cannot modify events or state. It's purely for observation.
 *
 * Multiple middleware instances can be attached to a single [Store],
 * and they will be notified in the order they were registered.
 *
 * @see Store
 * @see Actor
 */
public interface Middleware<Intent : Any, State : Any, SideEffect : Any> {

    /**
     * Called when the [Store] is initialized
     *
     * @param state The initial [State] of the [Store]
     */
   public fun onInit(state: State)

    /**
     * Called when the [Store] receives a new [Intent]
     *
     * This method is called before the [Actor] processes the intent,
     * allowing the middleware to observe the intent before any state changes occur.
     *
     * @param intent The received [Intent]
     * @param state The current [State] at the time the intent was received
     */
    public fun onIntent(intent: Intent, state: State)

    /**
     * Called when a new [State] is produced inside the [Actor]
     *
     * This method is called after a state reduction, providing both
     * the old and new states for comparison.
     *
     * @param oldState The previous [State] before the reduction
     * @param newState The new [State] after the reduction
     */
    public fun onStateChanged(oldState: State, newState: State)

    /**
     * Called when the [Actor] produces a new [SideEffect]
     *
     * This method is called when a side effect is emitted but before
     * it's delivered to collectors.
     *
     * @param sideEffect The emitted [SideEffect]
     * @param state The current [State] at the time the side effect was emitted
     */
    public fun onSideEffect(sideEffect: SideEffect, state: State)

    /**
     * Called when the [Store] is destroyed
     *
     * This is the last method called on the middleware, right before
     * the [Store] releases its resources. The [CoroutineScope] is still
     * active when this method is called.
     *
     * @param state The final [State] of the [Store]
     */
    public fun onDestroy(state: State)
}