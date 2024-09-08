package com.arttttt.simplemvi.store

import com.arttttt.simplemvi.store.actor.Actor
import com.arttttt.simplemvi.store.actor.ActorScope
import com.arttttt.simplemvi.store.actor.DefaultActor
import com.arttttt.simplemvi.store.actor.LoggingActor
import com.arttttt.simplemvi.store.logger.Logger
import kotlin.coroutines.CoroutineContext

operator fun <Intent> Store<Intent, *, *>.plus(intent: Intent) {
    accept(intent)
}

operator fun <Intent> Store<Intent, *, *>.plusAssign(intent: Intent) {
    accept(intent)
}

fun<Intent, State, SideEffect> createStore(
    initialState: State,
    actor: Actor<Intent, State, SideEffect>,
): Store<Intent, State, SideEffect> {
    return DefaultStore(
        initialState = initialState,
        actor = actor,
    )
}

fun <Intent, State, SideEffect> defaultActor(
    coroutineContext: CoroutineContext,
    block: ActorScope<Intent, State, SideEffect>.(intent: Intent) -> Unit
): Actor<Intent, State, SideEffect> {
    return DefaultActor(
        coroutineContext = coroutineContext,
        block = block,
    )
}

fun <Intent, State, SideEffect> loggingActor(
    name: String,
    logger: Logger,
    delegate: Actor<Intent, State, SideEffect>,
): Actor<Intent, State, SideEffect> {
    return LoggingActor(
        name = name,
        logger = logger,
        delegate = delegate,
    )
}