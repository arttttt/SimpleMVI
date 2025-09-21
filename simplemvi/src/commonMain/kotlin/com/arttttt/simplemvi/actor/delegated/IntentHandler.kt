package com.arttttt.simplemvi.actor.delegated

import com.arttttt.simplemvi.actor.ActorScope

public interface IntentHandler<Intent : Any, State : Any, SideEffect : Any, in I : Intent> {

    public fun ActorScope<Intent, State, SideEffect>.handle(intent: I)
}