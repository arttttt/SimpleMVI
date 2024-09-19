package com.arttttt.simplemvi.store

import com.arttttt.simplemvi.utils.MainThread
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface Store<in Intent : Any, out State : Any, out SideEffect : Any> {

    val states: StateFlow<State>

    val sideEffects: Flow<SideEffect>

    @MainThread
    fun init()

    @MainThread
    fun accept(intent: Intent)

    @MainThread
    fun destroy()
}