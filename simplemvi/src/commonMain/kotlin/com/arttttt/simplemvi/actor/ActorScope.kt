package com.arttttt.simplemvi.actor

import com.arttttt.simplemvi.utils.MainThread

interface ActorScope<in Intent : Any, State : Any, in SideEffect : Any> {

    val state: State

    @MainThread
    fun intent(intent: Intent)

    @MainThread
    fun reduce(block: State.() -> State)

    @MainThread
    fun sideEffect(sideEffect: SideEffect)
}