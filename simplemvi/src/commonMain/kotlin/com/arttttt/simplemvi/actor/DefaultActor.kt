package com.arttttt.simplemvi.actor

import com.arttttt.simplemvi.utils.mainthread.MainThread
import com.arttttt.simplemvi.utils.mainthread.assertOnMainThread
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext
import kotlin.properties.Delegates

abstract class DefaultActor<Intent : Any, State : Any, SideEffect : Any>(
    coroutineContext: CoroutineContext,
) : Actor<Intent, State, SideEffect> {

    private val scope = CoroutineScope(coroutineContext)

    private var actorScope: ActorScope<Intent, State, SideEffect> by Delegates.notNull()

    private val isInitialized = atomic(false)

    abstract fun handleIntent(intent: Intent)

    @MainThread
    override fun init(
        getState: () -> State,
        reduce: ((State) -> State) -> Unit,
        onNewIntent: (intent: Intent) -> Unit,
        postSideEffect: (sideEffect: SideEffect) -> Unit
    ) {
        assertOnMainThread()

        if (isInitialized.getAndSet(true)) return

        actorScope = object : ActorScope<Intent, State, SideEffect>, CoroutineScope by scope {

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
    }

    @MainThread
    override fun onIntent(intent: Intent) {
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
        actorScope.intent(intent)
    }

    @MainThread
    protected fun reduce(block: State.() -> State) {
        actorScope.reduce(block)
    }

    @MainThread
    protected fun sideEffect(sideEffect: SideEffect) {
        actorScope.sideEffect(sideEffect)
    }
}