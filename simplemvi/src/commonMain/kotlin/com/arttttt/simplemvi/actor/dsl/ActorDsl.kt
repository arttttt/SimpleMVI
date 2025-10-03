package com.arttttt.simplemvi.actor.dsl

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.actor.ActorScope
import com.arttttt.simplemvi.actor.delegated.DelegatedActor
import com.arttttt.simplemvi.actor.delegated.DestroyHandler
import com.arttttt.simplemvi.actor.delegated.InitHandler
import com.arttttt.simplemvi.actor.delegated.IntentHandler
import kotlin.reflect.KClass

/**
 * Creates a new [DelegatedActor] using a DSL builder
 *
 * This function provides a convenient DSL for defining actor behavior
 * without creating a custom [Actor] class. Intent handlers are registered
 * in a type-safe manner using the [ActorBuilder].
 *
 * Example:
 * ```
 * val actor = actorDsl<MyIntent, MyState, MySideEffect> {
 *     onInit {
 *         // Initialization logic
 *     }
 *
 *     onIntent<MyIntent.LoadData> { intent ->
 *         // Handle LoadData intent
 *         reduce { copy(loading = true) }
 *     }
 *
 *     onDestroy {
 *         // Cleanup logic
 *     }
 * }
 * ```
 *
 * @param block A lambda executed in the context of [ActorBuilder] to configure the actor
 * @return A configured [DelegatedActor] instance
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

/**
 * Creates a new [DelegatedActor] with explicitly provided handlers
 *
 * @param initHandler Handler for initialization logic. Defaults to no-op
 * @param intentHandlers List of intent handlers to register
 * @param destroyHandler Handler for destruction logic. Defaults to no-op
 * @return A configured [DelegatedActor] instance
 *
 * @see actorDsl
 * @see IntentHandler
 */
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

/**
 * Creates an [IntentHandler] for a specific intent type
 *
 * This is a helper function for creating individual intent handlers
 * that can be passed to [delegatedActor].
 *
 * Example:
 * ```
 * val loadDataHandler = intentHandler<MyIntent, MyState, MySideEffect, MyIntent.LoadData> { intent ->
 *     reduce { copy(loading = true) }
 *     // ... handle the intent
 * }
 * ```
 *
 * @param I The specific intent type this handler processes
 * @param block Lambda that handles the intent in the context of [ActorScope]
 * @return An [IntentHandler] for the specified intent type
 */
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