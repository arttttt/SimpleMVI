package com.arttttt.simplemvi.actor.delegated

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.actor.ActorScope
import kotlinx.coroutines.CoroutineScope
import kotlin.properties.Delegates
import kotlin.reflect.KClass

/**
 * An [Actor] implementation to be used within dsl
 */
public class DelegatedActor<Intent : Any, State : Any, SideEffect : Any>(
    private val initHandler: ActorScope<Intent, State, SideEffect>.() -> Unit,
    private val intentHandlers: Map<KClass<out Intent>, IntentHandler<Intent, State, SideEffect, in Intent>>,
    private val destroyHandler: ActorScope<Intent, State, SideEffect>.() -> Unit,
) : Actor<Intent, State, SideEffect> {

    private var actorScope: ActorScope<Intent, State, SideEffect> by Delegates.notNull()

    override fun init(
        scope: CoroutineScope,
        getState: () -> State,
        reduce: ((State) -> State) -> Unit,
        onNewIntent: (intent: Intent) -> Unit,
        postSideEffect: (sideEffect: SideEffect) -> Unit
    ) {
        actorScope = object : ActorScope<Intent, State, SideEffect> {

            override val state: State
                get() = getState()

            override val scope: CoroutineScope
                get() = scope

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

        with(handler) {
            actorScope.handle(intent)
        }
    }

    override fun destroy() {
        actorScope.apply(destroyHandler)
    }
}