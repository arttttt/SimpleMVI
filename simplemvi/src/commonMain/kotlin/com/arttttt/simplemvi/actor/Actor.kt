package com.arttttt.simplemvi.actor

interface Actor<Intent : Any, State : Any, out SideEffect : Any> {

    fun init(
        getState: () -> State,
        reduce: ((State) -> State) -> Unit,
        onNewIntent: (Intent) -> Unit,
        postSideEffect: (sideEffect: SideEffect) -> Unit,
    )

    fun onIntent(intent: Intent)

    fun destroy()
}