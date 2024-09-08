package com.arttttt.simplemvi.store

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface Store<in Intent, out State, out SideEffect> {

    val states: StateFlow<State>
    val state: State

    val sideEffects: Flow<SideEffect>

    fun init()

    fun accept(intent: Intent)

    fun destroy()
}