package com.arttttt.simplemvi.middleware

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.store.Store

/**
 * [Middleware] can be used when you to spy on the [Store]
 *
 * [Middleware] can not modify input events
 *
 * You can use [Middleware] for execution any extra code e.g. analytics, logging, etc
 *
 * @see Store
 * @see Actor
 */
public interface Middleware<Intent : Any, State : Any, SideEffect : Any> {

    /**
     * This function is called when the [Store] receives a new [Intent]
     */
    public fun onIntent(intent: Intent, state: State)

    /**
     * This function is called when a new [State] is produced inside the [Actor]
     */
    public fun onStateChanged(oldState: State, newState: State)

    /**
     * This function is called when the [Actor] produces a new [SideEffect]
     */
    public fun onSideEffect(sideEffect: SideEffect, state: State)
}