package com.arttttt.simplemvi.actor.dsl

import com.arttttt.simplemvi.actor.ActorScope
import com.arttttt.simplemvi.actor.delegated.DelegatedActor
import com.arttttt.simplemvi.actor.delegated.DestroyHandler
import com.arttttt.simplemvi.actor.delegated.InitHandler
import com.arttttt.simplemvi.actor.delegated.IntentHandler
import kotlin.reflect.KClass

/**
 * Builder class for configuring [DelegatedActor] using DSL
 *
 * This class provides methods to register handlers for various actor events:
 * - [onInit]: Called when the actor is initialized
 * - [onIntent]: Called when a specific intent type is received
 * - [onDestroy]: Called when the actor is destroyed
 *
 * The [ActorDslMarker] annotation ensures proper DSL scoping.
 *
 * @see DelegatedActor
 * @see actorDsl
 */
@ActorDslMarker
public class ActorBuilder<Intent : Any, State : Any, SideEffect: Any> {

    private val defaultInitHandler: InitHandler<Intent, State, SideEffect> = InitHandler {}
    private val defaultDestroyHandler: DestroyHandler<Intent, State, SideEffect> = DestroyHandler {}

    @PublishedApi
    internal var initHandler: InitHandler<Intent, State, SideEffect> = defaultInitHandler

    @PublishedApi
    internal val intentHandlers: MutableMap<KClass<out Intent>, IntentHandler<Intent, State, SideEffect, Intent>> = mutableMapOf()

    @PublishedApi
    internal var destroyHandler: DestroyHandler<Intent, State, SideEffect> = defaultDestroyHandler

    /**
     * This function registers a lambda that is called during [DelegatedActor] initialization
     *
     * @param block a labmda to be called during [DelegatedActor] initialization
     */
    public fun onInit(
        block: ActorScope<Intent, State, SideEffect>.() -> Unit,
    ) {
        require(initHandler === defaultInitHandler) {
            "init handler already registered"
        }

        initHandler = InitHandler(block)
    }

    /**
     * Registers an [Intent] handler for a specific intent type
     *
     * Each [Intent] type can have only one handler. Attempting to register
     * multiple handlers for the same intent type will throw an exception.
     *
     * The handler executes in the context of [ActorScope], providing access
     * to state management, side effect emission, and intent dispatching.
     *
     * Example:
     * ```
     * onIntent<MyIntent.LoadData> { intent ->
     *     reduce { copy(loading = true) }
     *     // ... handle the intent
     * }
     * ```
     *
     * @param T The specific intent type to handle
     * @param handler Lambda that handles the intent in the context of [ActorScope]
     * @throws IllegalArgumentException if a handler for this intent type is already registered
     */
    public inline fun <reified T : Intent> onIntent(
        crossinline handler: ActorScope<Intent, State, SideEffect>.(T) -> Unit,
    ) {
        require(!intentHandlers.containsKey(T::class)) {
            "intent handler already registered for ${T::class.simpleName}"
        }

        @Suppress("UNCHECKED_CAST")
        intentHandlers[T::class] = object : IntentHandler<Intent, State, SideEffect, T> {

            override val intentClass: KClass<T> = T::class

            override fun ActorScope<Intent, State, SideEffect>.handle(intent: T) {
                handler.invoke(this, intent)
            }
        } as IntentHandler<Intent, State, SideEffect, Intent>
    }

    /**
     * This function registers a lambda that is called when the [DelegatedActor] is about to be destroyed
     * It's still possible to launch a coroutine inside this functions
     *
     * @param block a labmda to be called when the [DelegatedActor] is about to be destroyed
     */
    public fun onDestroy(
        block: ActorScope<Intent, State, SideEffect>.() -> Unit,
    ) {
        require(destroyHandler === defaultDestroyHandler) {
            "destroy handler already registered"
        }

        destroyHandler = DestroyHandler(block)
    }
}