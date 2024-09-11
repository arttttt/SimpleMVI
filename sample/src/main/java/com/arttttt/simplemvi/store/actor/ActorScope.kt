package com.arttttt.simplemvi.store.actor

import kotlinx.coroutines.CoroutineScope

interface ActorScope<in Intent : Any, State : Any, in SideEffect : Any> {

    val scope: CoroutineScope

    fun getState(): State
    fun sideEffect(sideEffect: SideEffect)
    fun intent(intent: Intent)

    fun reduce(block: (state: State) -> State)
}