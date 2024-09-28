package com.arttttt.simplemvi.store

import com.arttttt.simplemvi.utils.MainThread
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

public interface Store<in Intent : Any, out State : Any, out SideEffect : Any> {

    public val state: State

    public val states: StateFlow<State>

    public val sideEffects: Flow<SideEffect>

    @MainThread
    public fun init()

    @MainThread
    public fun accept(intent: Intent)

    @MainThread
    public fun destroy()
}