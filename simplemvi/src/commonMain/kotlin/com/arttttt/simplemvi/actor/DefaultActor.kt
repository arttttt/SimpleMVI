package com.arttttt.simplemvi.actor

import com.arttttt.simplemvi.utils.MainThread
import com.arttttt.simplemvi.utils.assertOnMainThread
import kotlinx.coroutines.CoroutineScope
import kotlin.properties.Delegates

/**
 * Default actor implementation
 */
public abstract class DefaultActor<Intent : Any, State : Any, SideEffect : Any> : Actor<Intent, State, SideEffect> {

    protected val state: State
        get() = actorScope.state

    private var actorScope: ActorScope<Intent, State, SideEffect> by Delegates.notNull()
    protected var scope: CoroutineScope by Delegates.notNull()

    protected abstract fun handleIntent(intent: Intent)

    protected open fun onInit() {}
    protected open fun onDestroy() {}

    @MainThread
    final override fun init(
        scope: CoroutineScope,
        getState: () -> State,
        reduce: ((State) -> State) -> Unit,
        onNewIntent: (intent: Intent) -> Unit,
        postSideEffect: (sideEffect: SideEffect) -> Unit
    ) {
        assertOnMainThread()

        actorScope = object : ActorScope<Intent, State, SideEffect> {

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

        this.scope = scope

        onInit()
    }

    @MainThread
    final override fun onIntent(intent: Intent) {
        assertOnMainThread()

        handleIntent(intent)
    }

    @MainThread
    final override fun destroy() {
        assertOnMainThread()

        onDestroy()
    }

    @MainThread
    protected fun intent(intent: Intent) {
        assertOnMainThread()

        actorScope.intent(intent)
    }

    @MainThread
    protected fun reduce(block: State.() -> State) {
        assertOnMainThread()

        actorScope.reduce(block)
    }

    @MainThread
    protected fun sideEffect(sideEffect: SideEffect) {
        assertOnMainThread()

        actorScope.sideEffect(sideEffect)
    }
}