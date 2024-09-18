package com.arttttt.simplemvi.actor.dsl

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.utils.mainthread.MainThread
import com.arttttt.simplemvi.utils.mainthread.assertOnMainThread
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext
import kotlin.properties.Delegates
import kotlin.reflect.KClass

class DslActor<Intent : Any, State : Any, SideEffect : Any>(
    coroutineContext: CoroutineContext,
    private val initHandler: DslActorScope<Intent, State, SideEffect>.() -> Unit,
    private val intentHandlers: Map<KClass<out Intent>, DslActorScope<Intent, State, SideEffect>.(Intent) -> Unit>,
    private val destroyHandler: DslActorScope<Intent, State, SideEffect>.() -> Unit,
) : Actor<Intent, State, SideEffect> {

    protected val scope = CoroutineScope(coroutineContext)

    protected var actorScope: DslActorScope<Intent, State, SideEffect> by Delegates.notNull()

    protected val isInitialized = atomic(false)

    @MainThread
    override fun init(
        getState: () -> State,
        reduce: ((State) -> State) -> Unit,
        onNewIntent: (intent: Intent) -> Unit,
        postSideEffect: (sideEffect: SideEffect) -> Unit
    ) {
        assertOnMainThread()

        if (isInitialized.getAndSet(true)) return

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

        scope.cancel()
    }
}