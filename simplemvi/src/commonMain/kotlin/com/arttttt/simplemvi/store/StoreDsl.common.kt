package com.arttttt.simplemvi.store

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.middleware.Middleware
import kotlin.coroutines.CoroutineContext

public fun <Intent : Any, State : Any, SideEffect : Any> createStore(
    initialize: Boolean = true,
    coroutineContext: CoroutineContext,
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