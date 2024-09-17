package com.arttttt.simplemvi.actor

import com.arttttt.simplemvi.utils.mainthread.MainThread

interface Actor<Intent : Any, State : Any, out SideEffect : Any> {

    @MainThread
    fun init(
        getState: () -> State,
        reduce: (State.() -> State) -> Unit,
        onNewIntent: (Intent) -> Unit,
        postSideEffect: (sideEffect: SideEffect) -> Unit,
    )

    @MainThread
    fun onIntent(intent: Intent)

    @MainThread
    fun destroy()
}