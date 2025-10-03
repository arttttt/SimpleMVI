package com.arttttt.simplemvi.actor.delegated

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.actor.ActorScope
import kotlinx.coroutines.CoroutineScope

/**
 * Functional interface for handling [Actor] destruction
 *
 * This handler is executed once when the [Actor] is destroyed,
 * providing a chance to perform cleanup operations.
 * The [CoroutineScope] is still active when this handler is called.
 *
 * Example:
 * ```
 * val destroyHandler = DestroyHandler<MyIntent, MyState, MySideEffect> {
 *     // Cleanup logic here
 *     sideEffect(MySideEffect.ActorDestroyed)
 * }
 * ```
 *
 * @see DelegatedActor
 * @see ActorScope
 */
public fun interface DestroyHandler<Intent : Any, State : Any, SideEffect : Any> {

    /**
     * Called when the [Actor] is about to be destroyed
     *
     * Executed in the context of [ActorScope], providing access to
     * state management and side effect emission.
     */
    public fun ActorScope<Intent, State, SideEffect>.onDestroy()
}