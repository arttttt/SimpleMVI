package com.arttttt.simplemvi.plugin

import kotlinx.coroutines.CoroutineScope

public interface StorePlugin<Intent : Any, State : Any, SideEffect : Any> {

    public data class Context<Intent : Any, State : Any, SideEffect : Any>(
        val scope: CoroutineScope,
        val sendIntent: (Intent) -> Unit,
        val setState: (State) -> Unit,
        val sendSideEffect: (SideEffect) -> Unit,
        private val getState: () -> State,
    ) {

        val state: State
            get() = getState()
    }

    public fun provideInitialState(defaultState: State): State = defaultState

    public fun onInit(context: Context<Intent, State, SideEffect>) {}
    public fun Pipeline<Intent>.onIntent(intent: Intent) {}
    public fun onStateChanged(oldState: State, newState: State) {}
    public fun onSideEffect(sideEffect: SideEffect) {}
    public fun onDestroy() {}
}