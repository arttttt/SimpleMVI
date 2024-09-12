package com.arttttt.simplemvi.utils

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.actor.ActorScope
import com.arttttt.simplemvi.actor.DefaultActor
import com.arttttt.simplemvi.actor.LoggingActor
import com.arttttt.simplemvi.actor.dsl.ActorBuilder
import com.arttttt.simplemvi.logger.Logger
import com.arttttt.simplemvi.store.DefaultStore
import com.arttttt.simplemvi.store.Store
import kotlin.coroutines.CoroutineContext

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
    actor: Actor<Intent, State, SideEffect>,
): Store<Intent, State, SideEffect> {
    return DefaultStore(
        initialState = initialState,
        actor = actor,
    )
}