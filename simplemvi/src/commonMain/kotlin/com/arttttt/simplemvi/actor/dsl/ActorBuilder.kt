package com.arttttt.simplemvi.actor.dsl

import kotlin.reflect.KClass

@ActorDslMarker
class ActorBuilder<Intent : Any, State : Any, SideEffect: Any> {

    private val defaultInitHandler: DslActorScope<Intent, State, SideEffect>.() -> Unit = {}
    private val defaultDestroyHandler: DslActorScope<Intent, State, SideEffect>.() -> Unit = {}

    @PublishedApi
    internal var initHandler: DslActorScope<Intent, State, SideEffect>.() -> Unit = defaultInitHandler

    @PublishedApi
    internal val intentHandlers = mutableMapOf<KClass<out Intent>, DslActorScope<Intent, State, SideEffect>.(Intent) -> Unit>()

    @PublishedApi
    internal var destroyHandler: DslActorScope<Intent, State, SideEffect>.() -> Unit = defaultDestroyHandler

    fun init(
        block: DslActorScope<Intent, State, SideEffect>.() -> Unit,
    ) {
        require(initHandler === defaultInitHandler) {
            "init handler already registered"
        }

        initHandler = block
    }

    inline fun <reified T : Intent> onIntent(
        crossinline handler: DslActorScope<Intent, State, SideEffect>.(intent: T) -> Unit,
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
        block: DslActorScope<Intent, State, SideEffect>.() -> Unit,
    ) {
        require(destroyHandler === defaultDestroyHandler) {
            "destroy handler already registered"
        }

        destroyHandler = block
    }
}