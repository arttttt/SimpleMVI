package com.arttttt.simplemvi.actor

import com.arttttt.simplemvi.store.Store
import com.arttttt.simplemvi.utils.MainThread
import kotlinx.coroutines.CoroutineScope

/**
 * [Actor] is the place where you need to implement the logic
 *
 * [Actor] accepts [Intent] and can produce [SideEffect] and a new [State]
 *
 * [Actor] must be part of the [Store] and managed by it as well
 *
 * @see Store
 */
public interface Actor<Intent : Any, State : Any, out SideEffect : Any> {

    /**
     * This function initializes the [Actor]
     *
     * Called by the [Store]
     */
    @MainThread
    public fun init(
        scope: CoroutineScope,
        getState: () -> State,
        reduce: (State.() -> State) -> Unit,
        onNewIntent: (Intent) -> Unit,
        postSideEffect: (sideEffect: SideEffect) -> Unit,
    )

    /**
     * This function is called when the [Store] receives a new [Intent]
     *
     * Called by the [Store]
     */
    @MainThread
    public fun onIntent(intent: Intent)

    /**
     * This fuction destroyes the [Actor]
     *
     * Called by the [Store]
     */
    @MainThread
    public fun destroy()
}