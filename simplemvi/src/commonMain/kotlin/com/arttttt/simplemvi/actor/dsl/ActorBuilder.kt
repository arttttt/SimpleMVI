package com.arttttt.simplemvi.actor.dsl

import com.arttttt.simplemvi.actor.ActorScope
import com.arttttt.simplemvi.actor.delegated.DelegatedActor
import com.arttttt.simplemvi.actor.delegated.DestroyHandler
import com.arttttt.simplemvi.actor.delegated.InitHandler
import com.arttttt.simplemvi.actor.delegated.IntentHandler
import kotlin.reflect.KClass

/**
 * A helper class for the [DelegatedActor]
 *
 * @see DelegatedActor
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
     * This functions registers an [Intent] handler for each [Intent]
     * Each [Intent] can have only one handler
     *
     * @param handler a handler lambda to be called for a specific [Intent]
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