package com.arttttt.simplemvi.store.actor.dsl

import com.arttttt.simplemvi.store.actor.ActorScope
import kotlin.reflect.KClass

@ActorDslMarker
class ActorBuilder<Intent : Any, State : Any, SideEffect: Any> {

    @PublishedApi
    internal val intentHandlers = mutableMapOf<KClass<out Intent>, ActorScope<Intent, State, SideEffect>.(Intent) -> Boolean>()

    inline fun <reified T : Intent> onIntent(
        noinline handler: ActorScope<Intent, State, SideEffect>.(intent: T) -> Unit,
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
}