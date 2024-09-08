package com.arttttt.simplemvi.store.actor

interface Actor<in Intent, State, out SideEffect> {

    fun init(
        getState: () -> State,
        reduce: ((State) -> State) -> Unit,
        postSideEffect: (sideEffect: SideEffect) -> Unit,
    )

    fun onIntent(intent: Intent)

    fun destroy()
}