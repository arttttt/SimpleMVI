package com.arttttt.simplemvi.store

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.middleware.Middleware

fun <Intent : Any, State : Any, SideEffect : Any> createStore(
    initialize: Boolean = true,
    initialState: State,
    initialIntents: List<Intent> = emptyList(),
    middlewares: List<Middleware<Intent, State, SideEffect>> = emptyList(),
    actor: Actor<Intent, State, SideEffect>,
): Store<Intent, State, SideEffect> {
    return DefaultStore(
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