package com.arttttt.simplemvi.actor.delegated

import com.arttttt.simplemvi.actor.ActorScope
import kotlin.reflect.KClass

/**
 * Interface for handling specific [Intent] types
 *
 * Each handler is responsible for processing one specific intent type.
 * Handlers are registered with [DelegatedActor] and are invoked when
 * their corresponding intent type is received.
 *
 * Example:
 * ```
 * val loadDataHandler = object : IntentHandler<MyIntent, MyState, MySideEffect, MyIntent.LoadData> {
 *     override val intentClass = MyIntent.LoadData::class
 *
 *     override fun ActorScope<MyIntent, MyState, MySideEffect>.handle(intent: MyIntent.LoadData) {
 *         reduce { copy(loading = true) }
 *         // Handle the intent...
 *     }
 * }
 * ```
 *
 * @param Intent The base intent type
 * @param State The state type
 * @param SideEffect The side effect type
 * @param I The specific intent type this handler processes
 *
 * @see DelegatedActor
 * @see ActorScope
 */
public interface IntentHandler<Intent : Any, State : Any, SideEffect : Any, I : Intent> {

    /**
     * The Kotlin class of the intent type this handler processes
     *
     * Used by [DelegatedActor] to route intents to the correct handler.
     */
    public val intentClass: KClass<I>

    /**
     * Handles the received intent
     *
     * Executed in the context of [ActorScope], providing access to
     * state management, side effect emission, and intent dispatching.
     *
     * @param intent The intent instance to handle
     */
    public fun ActorScope<Intent, State, SideEffect>.handle(intent: I)
}