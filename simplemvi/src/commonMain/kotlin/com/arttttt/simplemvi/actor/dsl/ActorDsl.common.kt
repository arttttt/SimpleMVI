package com.arttttt.simplemvi.actor.dsl

import com.arttttt.simplemvi.actor.Actor
import kotlin.coroutines.CoroutineContext

public inline fun <Intent : Any, State : Any, SideEffect : Any> actorDsl(
    crossinline block: ActorBuilder<Intent, State, SideEffect>.() -> Unit,
): Actor<Intent, State, SideEffect> {
    val builder = ActorBuilder<Intent, State, SideEffect>()
    builder.block()

    return DslActor(
        initHandler = builder.initHandler,
        intentHandlers = builder.intentHandlers,
        destroyHandler = builder.destroyHandler,
    )
}