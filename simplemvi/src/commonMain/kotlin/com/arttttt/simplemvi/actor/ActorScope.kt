package com.arttttt.simplemvi.actor

import com.arttttt.simplemvi.utils.MainThread

public interface ActorScope<in Intent : Any, State : Any, in SideEffect : Any> {

    public val state: State

    @MainThread
    public fun intent(intent: Intent)

    @MainThread
    public fun reduce(block: State.() -> State)

    @MainThread
    public fun sideEffect(sideEffect: SideEffect)
}