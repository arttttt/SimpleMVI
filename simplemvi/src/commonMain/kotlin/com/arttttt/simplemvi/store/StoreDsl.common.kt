package com.arttttt.simplemvi.store

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.config.simpleMVIConfig
import com.arttttt.simplemvi.logging.LoggingMiddleware
import com.arttttt.simplemvi.middleware.Middleware
import com.arttttt.simplemvi.state.StateSaver
import com.arttttt.simplemvi.state.StateSaverMiddleware
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/**
 * Creates a new [Store] with given parameters
 *
 * @param name is a name of the [Store]
 * @param initialize is responsible for [Store] auto initialization
 * @param coroutineContext [CoroutineContext] to be used inside the [Store] for launching coroutines
 * @param initialState initial [State] of the [Store]
 * @param initialIntents a [List] of the initial [Intent] to start the [Store]
 * @param middlewares a [List] of the [Store] [Middleware]
 * @param actor an [Actor] to be used within the [Store]
 *
 * @see Store
 * @see Actor
 * @see Middleware
 */
public fun <Intent : Any, State : Any, SideEffect : Any> createStore(
    name: StoreName?,
    initialize: Boolean = true,
    coroutineContext: CoroutineContext = Dispatchers.Main.immediate,
    initialState: State,
    stateSaverFactory: ((StoreName) -> StateSaver<State>)? = null,
    initialIntents: List<Intent> = emptyList(),
    middlewares: List<Middleware<Intent, State, SideEffect>> = emptyList(),
    actor: Actor<Intent, State, SideEffect>,
): Store<Intent, State, SideEffect> {

    val stateSaver = stateSaverFactory?.let { factory -> name?.let(factory::invoke) }

    val realMiddlewares = buildList {
        if (name != null && simpleMVIConfig.logger != null) {
            add(
                LoggingMiddleware(
                    name = name.name,
                    logger = simpleMVIConfig.logger!!,
                )
            )
        }

        if (stateSaver != null) {
            add(StateSaverMiddleware(stateSaver))
        }

        addAll(middlewares)
    }

    return DefaultStore(
        coroutineContext = coroutineContext,
        initialState = stateSaver?.restoreState() ?: initialState,
        initialIntents = initialIntents,
        middlewares = realMiddlewares,
        actor = actor,
    ).apply {
        if (initialize) {
            init()
        }
    }
}