package com.arttttt.simplemvi.actor.dsl

import com.arttttt.simplemvi.actor.ActorScope
import kotlin.reflect.KClass

@ActorDslMarker
class ActorBuilder<Intent : Any, State : Any, SideEffect: Any> {

    @PublishedApi
    internal val intentHandlers = mutableMapOf<KClass<out Intent>, ActorScope<Intent, State, SideEffect>.(Intent) -> Boolean>()

    @PublishedApi
    internal var destroyHandler: ActorScope<Intent, State, SideEffect>.() -> Unit = {}

    inline fun <reified T : Intent> onIntent(
        crossinline handler: ActorScope<Intent, State, SideEffect>.(intent: T) -> Unit,
    ) {
        intentHandlers[T::class] = { intent ->
            if (intent is T) {
                handler(intent)
                true
            } else {
                false
            }
        }
    }

    fun onDestroy(
        block: ActorScope<Intent, State, SideEffect>.() -> Unit
    ) {
        destroyHandler = block
    }
}