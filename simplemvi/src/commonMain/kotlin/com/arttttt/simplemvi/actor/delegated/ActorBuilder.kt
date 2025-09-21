package com.arttttt.simplemvi.actor.delegated

import com.arttttt.simplemvi.actor.ActorScope
import kotlin.reflect.KClass

/**
 * A helper class for the [DelegatedActor]
 *
 * @see DelegatedActor
 */
@ActorDslMarker
public class ActorBuilder<Intent : Any, State : Any, SideEffect: Any> {

    private val defaultInitHandler: ActorScope<Intent, State, SideEffect>.() -> Unit = {}
    private val defaultDestroyHandler: ActorScope<Intent, State, SideEffect>.() -> Unit = {}

    @PublishedApi
    internal var initHandler: ActorScope<Intent, State, SideEffect>.() -> Unit = defaultInitHandler

    @PublishedApi
    internal val intentHandlers: MutableMap<KClass<out Intent>, ActorScope<Intent, State, SideEffect>.(Intent) -> Unit> = mutableMapOf()

    @PublishedApi
    internal var destroyHandler: ActorScope<Intent, State, SideEffect>.() -> Unit = defaultDestroyHandler

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

        initHandler = block
    }

    /**
     * This functions registers an [Intent] handler for each [Intent]
     * Each [Intent] can have only one handler
     *
     * @param handler a handler lambda to be called for a specific [Intent]
     */
    public inline fun <reified T : Intent> onIntent(
        crossinline handler: ActorScope<Intent, State, SideEffect>.(intent: T) -> Unit,
    ) {
        require(!intentHandlers.containsKey(T::class)) {
            "intent handler already registered for ${T::class.simpleName}"
        }

        intentHandlers[T::class] = { intent ->
            if (intent is T) {
                handler(intent)
             }
        }
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

        destroyHandler = block
    }
}