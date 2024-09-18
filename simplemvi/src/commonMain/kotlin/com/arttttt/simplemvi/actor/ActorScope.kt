package com.arttttt.simplemvi.actor

import com.arttttt.simplemvi.utils.mainthread.MainThread
import kotlinx.coroutines.CoroutineScope

interface ActorScope<in Intent : Any, State : Any, in SideEffect : Any> : CoroutineScope {

    val state: State

    @MainThread
    fun intent(intent: Intent)

    @MainThread
    fun reduce(block: State.() -> State)

    @MainThread
    fun sideEffect(sideEffect: SideEffect)
}