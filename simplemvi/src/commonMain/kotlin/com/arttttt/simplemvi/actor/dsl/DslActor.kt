package com.arttttt.simplemvi.actor.dsl

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.utils.MainThread
import com.arttttt.simplemvi.utils.assertOnMainThread
import kotlinx.coroutines.CoroutineScope
import kotlin.properties.Delegates
import kotlin.reflect.KClass

public class DslActor<Intent : Any, State : Any, SideEffect : Any>(
    private val initHandler: DslActorScope<Intent, State, SideEffect>.() -> Unit,
    private val intentHandlers: Map<KClass<out Intent>, DslActorScope<Intent, State, SideEffect>.(Intent) -> Unit>,
    private val destroyHandler: DslActorScope<Intent, State, SideEffect>.() -> Unit,
) : Actor<Intent, State, SideEffect> {

    private var actorScope: DslActorScope<Intent, State, SideEffect> by Delegates.notNull()

    @MainThread
    override fun init(
        scope: CoroutineScope,
        getState: () -> State,
        reduce: ((State) -> State) -> Unit,
        onNewIntent: (intent: Intent) -> Unit,
        postSideEffect: (sideEffect: SideEffect) -> Unit
    ) {
        assertOnMainThread()

        actorScope = object : DslActorScope<Intent, State, SideEffect>, CoroutineScope by scope {

            override val state: State
                get() = getState()

            override fun sideEffect(sideEffect: SideEffect) {
                postSideEffect(sideEffect)
            }

            @MainThread
            override fun intent(intent: Intent) {
                assertOnMainThread()

                onNewIntent(intent)
            }

            @MainThread
            override fun reduce(block: State.() -> State) {
                assertOnMainThread()

                reduce(block)
            }
        }

        actorScope.apply(initHandler)
    }

    @MainThread
    override fun onIntent(intent: Intent) {
        assertOnMainThread()

        val handler = intentHandlers[intent::class] ?: throw IllegalArgumentException("intent handler not found for $intent")

        handler.invoke(actorScope, intent)
    }

    @MainThread
    override fun destroy() {
        assertOnMainThread()

        actorScope.apply(destroyHandler)
    }
}