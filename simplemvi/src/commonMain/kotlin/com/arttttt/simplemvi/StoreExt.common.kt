package com.arttttt.simplemvi

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.actor.ActorScope
import com.arttttt.simplemvi.actor.DefaultActor
import com.arttttt.simplemvi.actor.LoggingActor
import com.arttttt.simplemvi.actor.dsl.ActorBuilder
import com.arttttt.simplemvi.logger.Logger
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

fun <Intent : Any, State : Any, SideEffect : Any> defaultActor(
    coroutineContext: CoroutineContext,
    block: ActorScope<Intent, State, SideEffect>.(intent: Intent) -> Unit
): Actor<Intent, State, SideEffect> {
    return object : DefaultActor<Intent, State, SideEffect>(
        coroutineContext = coroutineContext,
        block = block,
    ) {}
}

fun <Intent : Any, State : Any, SideEffect : Any> loggingActor(
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

fun <Intent : Any, State : Any, SideEffect : Any> actorDsl(
    coroutineContext: CoroutineContext,
    block: ActorBuilder<Intent, State, SideEffect>.() -> Unit
): Actor<Intent, State, SideEffect> {
    val builder = ActorBuilder<Intent, State, SideEffect>()
    builder.block()

    return object : DefaultActor<Intent, State, SideEffect>(
        coroutineContext = coroutineContext,
        block = { intent ->
            builder
                .intentHandlers[intent::class]
                ?.invoke(this, intent)
                ?: throw IllegalArgumentException("intent handler not found for $intent")
        }
    ) {
        override fun destroy() {
            super.destroy()

            builder.destroyHandler(actorScope)
        }
    }
}