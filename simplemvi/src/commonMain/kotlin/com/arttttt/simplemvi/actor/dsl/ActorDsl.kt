package com.arttttt.simplemvi.actor.dsl

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.actor.ActorScope
import com.arttttt.simplemvi.actor.delegated.DelegatedActor
import com.arttttt.simplemvi.actor.delegated.DestroyHandler
import com.arttttt.simplemvi.actor.delegated.InitHandler
import com.arttttt.simplemvi.actor.delegated.IntentHandler
import kotlin.reflect.KClass

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

public fun <Intent : Any, State : Any, SideEffect : Any> delegatedActor(
    initHandler: InitHandler<Intent, State, SideEffect> = InitHandler {},
    intentHandlers: List<IntentHandler<Intent, State, SideEffect, out Intent>>,
    destroyHandler: DestroyHandler<Intent, State, SideEffect> = DestroyHandler {},
): Actor<Intent, State, SideEffect> {
    return DelegatedActor(
        initHandler = initHandler,
        intentHandlers = intentHandlers.associateBy { intentHandler -> intentHandler.intentClass },
        destroyHandler = destroyHandler,
    )
}

public inline fun <Intent : Any, State : Any, SideEffect : Any, reified I : Intent> intentHandler(
    crossinline block: ActorScope<Intent, State, SideEffect>.(I) -> Unit
): IntentHandler<Intent, State, SideEffect, I> {
    return object : IntentHandler<Intent, State, SideEffect, I> {

        override val intentClass: KClass<I> = I::class

        override fun ActorScope<Intent, State, SideEffect>.handle(intent: I) {
            block(intent)
        }
    }
}