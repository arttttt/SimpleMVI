package com.arttttt.simplemvi.actor.delegated

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.actor.ActorScope

/**
 * Functional interface for handling [Actor] initialization
 *
 * This handler is executed once when the [Actor] is initialized,
 * providing a chance to perform setup operations.
 *
 * Example:
 * ```
 * val initHandler = InitHandler<MyIntent, MyState, MySideEffect> {
 *     // Setup logic here
 *     reduce { copy(initialized = true) }
 * }
 * ```
 *
 * @see DelegatedActor
 * @see ActorScope
 */
public fun interface InitHandler<Intent : Any, State : Any, SideEffect : Any> {

    /**
     * Called when the [Actor] is initialized
     *
     * Executed in the context of [ActorScope], providing access to
     * state management and side effect emission.
     */
    public fun ActorScope<Intent, State, SideEffect>.onInit()
}