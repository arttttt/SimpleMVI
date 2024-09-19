package com.arttttt.simplemvi.middleware

public interface Middleware<Intent : Any, State : Any, SideEffect : Any> {

    public fun onIntent(intent: Intent, state: State)

    public fun onStateChanged(oldState: State, newState: State)

    public fun onSideEffect(sideEffect: SideEffect, state: State)
}