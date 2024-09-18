package com.arttttt.simplemvi.actor.dsl

import com.arttttt.simplemvi.actor.ActorScope

@ActorDslMarker
interface DslActorScope<in Intent : Any, State : Any, in SideEffect : Any> : ActorScope<Intent, State, SideEffect>