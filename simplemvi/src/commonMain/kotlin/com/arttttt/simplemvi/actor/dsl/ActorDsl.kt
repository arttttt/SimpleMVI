package com.arttttt.simplemvi.actor.dsl

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.actor.delegated.DelegatedActor

/**
 * Creates a new [com.arttttt.simplemvi.actor.delegated.DelegatedActor]
 *
 * @param block a lambda to be called within [ActorBuilder]
 *
 * @see DelegatedActor
 * @see ActorBuilder
 */
public inline fun <Intent : Any, State : Any, SideEffect : Any> actorDsl(
    crossinline block: ActorBuilder<Intent, State, SideEffect>.() -> Unit,
): Actor<Intent, State, SideEffect> {
    val builder = ActorBuilder<Intent, State, SideEffect>().apply(block)

    return DelegatedActor(
        initHandler = builder.initHandler,
        intentHandlers = builder.intentHandlers,
        destroyHandler = builder.destroyHandler,
    )
}