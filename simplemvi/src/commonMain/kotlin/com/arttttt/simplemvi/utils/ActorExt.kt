package com.arttttt.simplemvi.utils

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.actor.ActorScope
import com.arttttt.simplemvi.actor.DefaultActor
import com.arttttt.simplemvi.actor.dsl.ActorBuilder
import com.arttttt.simplemvi.actor.dsl.DslActor
import kotlin.coroutines.CoroutineContext

fun <Intent : Any, State : Any, SideEffect : Any> defaultActor(
    coroutineContext: CoroutineContext,
    block: ActorScope<Intent, State, SideEffect>.(intent: Intent) -> Unit
): Actor<Intent, State, SideEffect> {
    return object : DefaultActor<Intent, State, SideEffect>(
        coroutineContext = coroutineContext,
        block = block,
    ) {}
}

inline fun <Intent : Any, State : Any, SideEffect : Any> actorDsl(
    coroutineContext: CoroutineContext,
    crossinline block: ActorBuilder<Intent, State, SideEffect>.() -> Unit,
): Actor<Intent, State, SideEffect> {
    val builder = ActorBuilder<Intent, State, SideEffect>()
    builder.block()

    return DslActor(
        coroutineContext = coroutineContext,
        initHandler = builder.initHandler,
        intentHandlers = builder.intentHandlers,
        destroyHandler = builder.destroyHandler,
    )
}