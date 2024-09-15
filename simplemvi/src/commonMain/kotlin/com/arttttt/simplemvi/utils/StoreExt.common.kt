package com.arttttt.simplemvi.utils

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.store.DefaultStore
import com.arttttt.simplemvi.store.Store

operator fun <Intent : Any> Store<Intent, *, *>.plus(intent: Intent) {
    accept(intent)
}

operator fun <Intent : Any> Store<Intent, *, *>.plusAssign(intent: Intent) {
    accept(intent)
}

val <State : Any>Store<*, State, *>.state: State
    get() {
        return states.value
    }

fun <Intent : Any, State : Any, SideEffect : Any> createStore(
    initialState: State,
    initialIntents: List<Intent>,
    actor: Actor<Intent, State, SideEffect>,
): Store<Intent, State, SideEffect> {
    return DefaultStore(
        initialState = initialState,
        initialIntents = initialIntents,
        actor = actor,
    )
}