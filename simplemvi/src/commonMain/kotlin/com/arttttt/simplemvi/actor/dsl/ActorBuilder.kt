package com.arttttt.simplemvi.actor.dsl

import com.arttttt.simplemvi.actor.ActorScope
import kotlin.reflect.KClass

@ActorDslMarker
class ActorBuilder<Intent : Any, State : Any, SideEffect: Any> {

    private val defaultInitHandler: ActorScope<Intent, State, SideEffect>.() -> Unit = {}
    private val defaultDestroyHandler: ActorScope<Intent, State, SideEffect>.() -> Unit = {}

    @PublishedApi
    internal var initHandler: ActorScope<Intent, State, SideEffect>.() -> Unit = defaultInitHandler

    @PublishedApi
    internal val intentHandlers = mutableMapOf<KClass<out Intent>, ActorScope<Intent, State, SideEffect>.(Intent) -> Unit>()

    @PublishedApi
    internal var destroyHandler: ActorScope<Intent, State, SideEffect>.() -> Unit = defaultDestroyHandler

    fun init(
        block: ActorScope<Intent, State, SideEffect>.() -> Unit,
    ) {
        require(initHandler === defaultInitHandler) {
            "init handler already registered"
        }

        initHandler = block
    }

    inline fun <reified T : Intent> onIntent(
        crossinline handler: ActorScope<Intent, State, SideEffect>.(intent: T) -> Unit,
    ) {
        require(!intentHandlers.containsKey(T::class)) {
            "intent handler already registered for ${T::class.qualifiedName}"
        }

        intentHandlers[T::class] = { intent ->
            if (intent is T) {
                handler(intent)
             }
        }
    }

    fun onDestroy(
        block: ActorScope<Intent, State, SideEffect>.() -> Unit,
    ) {
        require(destroyHandler === defaultDestroyHandler) {
            "destroy handler already registered"
        }

        destroyHandler = block
    }
}