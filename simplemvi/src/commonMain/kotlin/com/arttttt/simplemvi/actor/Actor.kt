package com.arttttt.simplemvi.actor

import com.arttttt.simplemvi.utils.MainThread
import kotlinx.coroutines.CoroutineScope

public interface Actor<Intent : Any, State : Any, out SideEffect : Any> {

    @MainThread
    public fun init(
        scope: CoroutineScope,
        getState: () -> State,
        reduce: (State.() -> State) -> Unit,
        onNewIntent: (Intent) -> Unit,
        postSideEffect: (sideEffect: SideEffect) -> Unit,
    )

    @MainThread
    public fun onIntent(intent: Intent)

    @MainThread
    public fun destroy()
}