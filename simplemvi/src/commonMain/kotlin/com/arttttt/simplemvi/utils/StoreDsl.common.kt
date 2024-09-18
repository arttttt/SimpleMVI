package com.arttttt.simplemvi.utils

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.middleware.Middleware
import com.arttttt.simplemvi.store.DefaultStore
import com.arttttt.simplemvi.store.Store

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