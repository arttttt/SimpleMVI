package com.arttttt.simplemvi.actor

import com.arttttt.simplemvi.store.Store
import kotlinx.coroutines.CoroutineScope
import kotlin.properties.Delegates

/**
 * Default [Actor] implementation
 */
public abstract class DefaultActor<Intent : Any, State : Any, SideEffect : Any> : Actor<Intent, State, SideEffect> {

    /**
     * Provides access to the current [State] of the [Store]
     */
    protected val state: State
        get() = actorScope.state

    private var actorScope: ActorScope<Intent, State, SideEffect> by Delegates.notNull()

    /**
     * Provides access to the [CoroutineScope] of the [Store]
     */
    protected val scope: CoroutineScope
        get() {
            return actorScope.scope
        }

    /**
     * Handles the received [Intent]
     *
     * This is the main method where business logic should be implemented.
     * Override this method to define how each intent type should be processed.
     *
     * @param intent The [Intent] to be handled
     */
    protected abstract fun handleIntent(intent: Intent)

    /**
     * Called when the [Actor] is initialized
     *
     * Override this method to perform any initialization logic.
     * The [CoroutineScope] is already available at this point.
     */
    protected open fun onInit() {}

    /**
     * Called when the [Actor] is about to be destroyed
     *
     * Override this method to perform cleanup operations.
     * The [CoroutineScope] is still active when this method is called.
     */
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

    /**
     * Dispatches a new [Intent] to be processed by this [Actor]
     *
     * Use this method when handling one intent needs to trigger another intent.
     *
     * @param intent The [Intent] to dispatch
     */
    protected fun intent(intent: Intent) {
        actorScope.intent(intent)
    }

    /**
     * Updates the [State] of the [Store]
     *
     * @param block A lambda that receives the current [State] and returns the new [State]
     */
    protected fun reduce(block: State.() -> State) {
        actorScope.reduce(block)
    }

    /**
     * Emits a [SideEffect] from the [Actor]
     *
     * Side effects represent one-time events that cannot be represented in the state.
     *
     * @param sideEffect The [SideEffect] to emit
     */
    protected fun sideEffect(sideEffect: SideEffect) {
        actorScope.sideEffect(sideEffect)
    }
}