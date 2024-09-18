package com.arttttt.simplemvi.middleware

interface Middleware<Intent : Any, State : Any, SideEffect : Any> {

    fun onIntent(intent: Intent, state: State)

    fun onStateChanged(oldState: State, newState: State)

    fun onSideEffect(sideEffect: SideEffect, state: State)
}