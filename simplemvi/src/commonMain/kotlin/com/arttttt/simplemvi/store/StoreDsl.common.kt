package com.arttttt.simplemvi.store

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.middleware.Middleware
import com.arttttt.simplemvi.plugin.PluginContext
import com.arttttt.simplemvi.plugin.PluginsConfigurator
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
    initialIntents: List<Intent> = emptyList(),
    middlewares: List<Middleware<Intent, State, SideEffect>> = emptyList(),
    plugins: (PluginsConfigurator<Intent, State, SideEffect>.() -> Unit)? = null,
    actor: Actor<Intent, State, SideEffect>,
): Store<Intent, State, SideEffect> {

    val pluginContext = PluginContext<Intent, State, SideEffect>(initialState)
    val configurator = PluginsConfigurator<Intent, State, SideEffect>()

    plugins?.invoke(configurator)

    configurator.installedPlugins.forEach { plugin ->
        plugin.install(
            name = name,
            context = pluginContext,
        )
    }

    val finalInitialState = pluginContext.initialState

    val realMiddlewares = buildList {
        addAll(pluginContext.middlewares)
        addAll(middlewares)
    }

    return DefaultStore(
        coroutineContext = coroutineContext,
        initialState = finalInitialState,
        initialIntents = initialIntents,
        middlewares = realMiddlewares,
        actor = actor,
    ).apply {
        if (initialize) {
            init()
        }
    }
}