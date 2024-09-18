package com.arttttt.simplemvi.middleware

import com.arttttt.simplemvi.utils.mainthread.MainThread

interface Middleware<Intent : Any, State : Any, SideEffect : Any> {

    @MainThread
    fun onIntent(intent: Intent, state: State)

    @MainThread
    fun onStateChanged(oldState: State, newState: State)

    @MainThread
    fun onSideEffect(sideEffect: SideEffect, state: State)
}