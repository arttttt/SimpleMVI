package com.arttttt.simplemvi.store

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.middleware.Middleware
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/**
 * Creates a new [Store] with given parameters
 *
 * @param initialize is responsible for [Store] auto initialization
 * @param coroutineContext [CoroutineContext] to be used inside the [Store] for launching coroutines
 * @param initialState initial [State] of the [Store]
 * @param initialIntents a [List] of the initial [Intent] to start the [Store]
 * @param middlewares a [List] of the [Store] [Middleware]
 * @param actor an [Actor] to be used within the [Store]
 *
 * @see Store
 * @see Actor
 * @see Middleware
 */
public fun <Intent : Any, State : Any, SideEffect : Any> createStore(
    initialize: Boolean = true,
    coroutineContext: CoroutineContext = Dispatchers.Main.immediate,
    initialState: State,
    initialIntents: List<Intent> = emptyList(),
    middlewares: List<Middleware<Intent, State, SideEffect>> = emptyList(),
    actor: Actor<Intent, State, SideEffect>,
): Store<Intent, State, SideEffect> {
    return DefaultStore(
        coroutineContext = coroutineContext,
        initialState = initialState,
        initialIntents = initialIntents,
        middlewares = middlewares,
        actor = actor,
    ).apply {
        if (initialize) {
            init()
        }
    }
}