package com.arttttt.simplemvi.actor

import com.arttttt.simplemvi.utils.MainThread

public interface Actor<Intent : Any, State : Any, out SideEffect : Any> {

    @MainThread
    public fun init(
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