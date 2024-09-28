package com.arttttt.simplemvi.actor.dsl

import com.arttttt.simplemvi.actor.ActorScope
import kotlinx.coroutines.CoroutineScope

/**
 * The [DslActorScope] is used inside [DslActor]
 * It extends both [ActorScope] and [CoroutineScope]
 *
 * @see ActorScope
 * @see DslActor
 */
@ActorDslMarker
public interface DslActorScope<in Intent : Any, State : Any, in SideEffect : Any> : ActorScope<Intent, State, SideEffect>,
    CoroutineScope