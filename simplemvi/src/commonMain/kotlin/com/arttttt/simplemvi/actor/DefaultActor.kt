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
    private val block: ActorScope<Intent, State, SideEffect>.(intent: Intent) -> Unit
) : Actor<Intent, State, SideEffect> {

    protected val scope = CoroutineScope(coroutineContext)

    protected var actorScope: ActorScope<Intent, State, SideEffect> by Delegates.notNull()

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

        actorScope = object : ActorScope<Intent, State, SideEffect>, CoroutineScope by scope {

            override fun getState(): State {
                return getState()
            }

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

        actorScope.block(intent)
    }

    @MainThread
    override fun destroy() {
        assertOnMainThread()

        scope.cancel()
    }
}