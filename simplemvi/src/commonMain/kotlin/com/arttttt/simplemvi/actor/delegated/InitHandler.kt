package com.arttttt.simplemvi.actor.delegated

import com.arttttt.simplemvi.actor.ActorScope

public fun interface InitHandler<Intent : Any, State : Any, SideEffect : Any> {

    public fun ActorScope<Intent, State, SideEffect>.onInit()
}