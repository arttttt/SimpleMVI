package com.arttttt.simplemvi.store

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.config.simpleMVIConfig
import com.arttttt.simplemvi.logging.LoggingPlugin
import com.arttttt.simplemvi.plugin.StorePlugin
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
 * @param plugins a [List] of the [StorePlugin]
 * @param actor an [Actor] to be used within the [Store]
 *
 * @see Store
 * @see Actor
 * @see StorePlugin
 */
public fun <Intent : Any, State : Any, SideEffect : Any> createStore(
    name: StoreName?,
    initialize: Boolean = true,
    coroutineContext: CoroutineContext = Dispatchers.Main.immediate,
    initialState: State,
    initialIntents: List<Intent> = emptyList(),
    plugins: List<StorePlugin<Intent, State, SideEffect>> = emptyList(),
    actor: Actor<Intent, State, SideEffect>,
): Store<Intent, State, SideEffect> {
    val realPlugins = buildList<StorePlugin<Intent, State, SideEffect>> {
        if (name != null && simpleMVIConfig.logger != null) {
            this += LoggingPlugin(
                name = name.name,
                logger = simpleMVIConfig.logger!!,
            )
        }

        this += plugins
    }

    return DefaultStore(
        coroutineContext = coroutineContext,
        initialState = initialState,
        initialIntents = initialIntents,
        plugins = realPlugins,
        actor = actor,
    ).apply {
        if (initialize) {
            init()
        }
    }
}