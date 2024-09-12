package com.arttttt.simplemvi.actor

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext
import kotlin.properties.Delegates

abstract class DefaultActor<Intent : Any, State : Any, SideEffect : Any>(
    coroutineContext: CoroutineContext,
    private val block: ActorScope<Intent, State, SideEffect>.(intent: Intent) -> Unit
) : Actor<Intent, State, SideEffect> {

    protected val scope = CoroutineScope(coroutineContext)

    protected var actorScope: ActorScope<Intent, State, SideEffect> by Delegates.notNull()

    protected val isInitialized = atomic(false)

    override fun init(
        getState: () -> State,
        reduce: ((State) -> State) -> Unit,
        onNewIntent: (intent: Intent) -> Unit,
        postSideEffect: (sideEffect: SideEffect) -> Unit
    ) {
        if (isInitialized.value) return

        isInitialized.value = true

        actorScope = object : ActorScope<Intent, State, SideEffect> {
            override val scope: CoroutineScope = this@DefaultActor.scope

            override fun getState(): State {
                return getState()
            }

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
    }

    override fun onIntent(intent: Intent) {
        actorScope.block(intent)
    }

    override fun destroy() {
        scope.cancel()
    }
}