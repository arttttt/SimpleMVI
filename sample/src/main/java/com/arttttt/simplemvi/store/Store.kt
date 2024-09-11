package com.arttttt.simplemvi.store

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface Store<in Intent : Any, out State : Any, out SideEffect : Any> {

    val states: StateFlow<State>

    val sideEffects: Flow<SideEffect>

    fun init()

    fun accept(intent: Intent)

    fun destroy()
}