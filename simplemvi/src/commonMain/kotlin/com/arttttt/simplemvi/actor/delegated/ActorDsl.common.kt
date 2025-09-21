package com.arttttt.simplemvi.actor.delegated

import com.arttttt.simplemvi.actor.Actor

/**
 * Creates a new [DelegatedActor]
 *
 * @param block a lambda to be called within [ActorBuilder]
 *
 * @see DelegatedActor
 * @see ActorBuilder
 */
public inline fun <Intent : Any, State : Any, SideEffect : Any> actorDsl(
    crossinline block: ActorBuilder<Intent, State, SideEffect>.() -> Unit,
): Actor<Intent, State, SideEffect> {
    val builder = ActorBuilder<Intent, State, SideEffect>()
    builder.block()

    return DelegatedActor(
        initHandler = builder.initHandler,
        intentHandlers = builder.intentHandlers,
        destroyHandler = builder.destroyHandler,
    )
}