package com.arttttt.simplemvi.actor

import com.arttttt.simplemvi.utils.MainThread
import com.arttttt.simplemvi.utils.assertOnMainThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext
import kotlin.properties.Delegates

public abstract class DefaultActor<Intent : Any, State : Any, SideEffect : Any>(
    coroutineContext: CoroutineContext,
) : Actor<Intent, State, SideEffect> {

    protected val scope: CoroutineScope = CoroutineScope(coroutineContext)

    protected val state: State
        get() = actorScope.state

    private var actorScope: ActorScope<Intent, State, SideEffect> by Delegates.notNull()

    protected abstract fun handleIntent(intent: Intent)

    protected open fun onInit() {}

    @MainThread
    final override fun init(
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

        onInit()
    }

    @MainThread
    final override fun onIntent(intent: Intent) {
        assertOnMainThread()

        handleIntent(intent)
    }

    @MainThread
    override fun destroy() {
        assertOnMainThread()

        scope.cancel()
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