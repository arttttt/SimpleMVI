package com.arttttt.simplemvi.actor.dsl

import com.arttttt.simplemvi.actor.Actor
import kotlinx.coroutines.CoroutineScope
import kotlin.properties.Delegates
import kotlin.reflect.KClass

/**
 * An [Actor] implementation to be used within dsl
 */
public class DslActor<Intent : Any, State : Any, SideEffect : Any>(
    private val initHandler: DslActorScope<Intent, State, SideEffect>.() -> Unit,
    private val intentHandlers: Map<KClass<out Intent>, DslActorScope<Intent, State, SideEffect>.(Intent) -> Unit>,
    private val destroyHandler: DslActorScope<Intent, State, SideEffect>.() -> Unit,
) : Actor<Intent, State, SideEffect> {

    private var actorScope: DslActorScope<Intent, State, SideEffect> by Delegates.notNull()

    override fun init(
        scope: CoroutineScope,
        getState: () -> State,
        reduce: ((State) -> State) -> Unit,
        onNewIntent: (intent: Intent) -> Unit,
        postSideEffect: (sideEffect: SideEffect) -> Unit
    ) {
        actorScope = object : DslActorScope<Intent, State, SideEffect>, CoroutineScope by scope {

            override val state: State
                get() = getState()

            override fun sideEffect(sideEffect: SideEffect) {
                postSideEffect(sideEffect)
            }

            override fun intent(intent: Intent) {
                onNewIntent(intent)
            }

            override fun reduce(block: State.() -> State) {
                reduce(block)
            }
        }

        actorScope.apply(initHandler)
    }

    override fun onIntent(intent: Intent) {
        val handler = intentHandlers[intent::class] ?: throw IllegalArgumentException("intent handler not found for $intent")

        handler.invoke(actorScope, intent)
    }

    override fun destroy() {
        actorScope.apply(destroyHandler)
    }
}