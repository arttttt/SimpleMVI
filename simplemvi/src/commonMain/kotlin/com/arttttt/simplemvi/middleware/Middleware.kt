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
    * this function is called when the [Store] is initialized
    *
    * @param state - current [Store] state
    */
   public fun onInit(state: State)

    /**
     * This function is called when the [Store] receives a new [Intent]
     *
     * @param intent - received intent
     * @param state - current [Store] state
     */
    public fun onIntent(intent: Intent, state: State)

    /**
     * This function is called when a new [State] is produced inside the [Actor]
     *
     * @param oldState - old [Store] state
     * @param newState - new [Store] state
     */
    public fun onStateChanged(oldState: State, newState: State)

    /**
     * This function is called when the [Actor] produces a new [SideEffect]
     *
     * @param sideEffect - emitted SideEffect event
     * @param state - current [Store] state
     */
    public fun onSideEffect(sideEffect: SideEffect, state: State)

    /**
     * this function is called when the [Store] is destroyed
     *
     * @param state - current [Store] state
     */
    public fun onDestroy(state: State)
}