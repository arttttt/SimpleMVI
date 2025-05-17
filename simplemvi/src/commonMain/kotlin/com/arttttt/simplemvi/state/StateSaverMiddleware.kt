package com.arttttt.simplemvi.state

import com.arttttt.simplemvi.middleware.Middleware

public class StateSaverMiddleware<Intent : Any, State : Any, SideEffect : Any>(
    private val stateSaver: StateSaver<State>
) : Middleware<Intent, State, SideEffect> {
    override fun onInit(state: State) {
        stateSaver.saveState(state)
    }

    override fun onIntent(intent: Intent, state: State) {}

    override fun onStateChanged(oldState: State, newState: State) {
        stateSaver.saveState(newState)
    }

    override fun onSideEffect(sideEffect: SideEffect, state: State) {}

    override fun onDestroy(state: State) {}
}