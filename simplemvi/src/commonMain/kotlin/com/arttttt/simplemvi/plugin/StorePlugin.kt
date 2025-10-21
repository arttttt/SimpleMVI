package com.arttttt.simplemvi.plugin

import com.arttttt.simplemvi.store.StoreName
import kotlinx.coroutines.CoroutineScope

public interface StorePlugin<Intent : Any, State : Any, SideEffect : Any> {

    public data class Context<Intent : Any, State : Any, SideEffect : Any>(
        val scope: CoroutineScope,
        val name: StoreName?,
        val getCurrentState: () -> State,
        val sendIntent: (Intent) -> Unit,
        val setState: (State) -> Unit,
    )

    public fun provideInitialState(defaultState: State): State = defaultState

    public fun onInit(context: Context<Intent, State, SideEffect>) {}
    public fun onIntent(intent: Intent) {}
    public fun onStateChanged(oldState: State, newState: State) {}
    public fun onSideEffect(sideEffect: SideEffect) {}
    public fun onDestroy() {}
}