package com.arttttt.simplemvi.store.actor

interface Actor<Intent, State, out SideEffect> {

    fun init(
        getState: () -> State,
        reduce: ((State) -> State) -> Unit,
        onNewIntent: (Intent) -> Unit,
        postSideEffect: (sideEffect: SideEffect) -> Unit,
    )

    fun onIntent(intent: Intent)

    fun destroy()
}