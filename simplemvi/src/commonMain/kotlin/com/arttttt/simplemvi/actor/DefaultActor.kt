package com.arttttt.simplemvi.actor

import kotlinx.coroutines.CoroutineScope
import kotlin.properties.Delegates

/**
 * Default actor implementation
 */
public abstract class DefaultActor<Intent : Any, State : Any, SideEffect : Any> : Actor<Intent, State, SideEffect> {

    protected val state: State
        get() = actorScope.state

    private var actorScope: ActorScope<Intent, State, SideEffect> by Delegates.notNull()

    protected val scope: CoroutineScope
        get() {
            return actorScope.scope
        }

    protected abstract fun handleIntent(intent: Intent)

    protected open fun onInit() {}
    protected open fun onDestroy() {}

    final override fun init(
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

        onInit()
    }

    final override fun onIntent(intent: Intent) {
        handleIntent(intent)
    }

    final override fun destroy() {
        onDestroy()
    }

    protected fun intent(intent: Intent) {
        actorScope.intent(intent)
    }

    protected fun reduce(block: State.() -> State) {
        actorScope.reduce(block)
    }

    protected fun sideEffect(sideEffect: SideEffect) {
        actorScope.sideEffect(sideEffect)
    }
}