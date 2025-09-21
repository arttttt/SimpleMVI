package com.arttttt.simplemvi.actor.delegated

import com.arttttt.simplemvi.actor.ActorScope
import kotlin.reflect.KClass

public interface IntentHandler<Intent : Any, State : Any, SideEffect : Any, I : Intent> {

    public val intentClass: KClass<I>

    public fun ActorScope<Intent, State, SideEffect>.handle(intent: I)
}