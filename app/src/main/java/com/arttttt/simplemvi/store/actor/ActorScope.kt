package com.arttttt.simplemvi.store.actor

import kotlinx.coroutines.CoroutineScope

interface ActorScope<in Intent, State, in SideEffect> {

    val scope: CoroutineScope

    fun getState(): State
    fun sideEffect(sideEffect: SideEffect)
    fun intent(intent: Intent)

    fun reduce(block: State.() -> State)
}