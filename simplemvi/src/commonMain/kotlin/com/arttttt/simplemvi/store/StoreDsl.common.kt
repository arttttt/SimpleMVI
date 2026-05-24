package com.arttttt.simplemvi.store

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.config.simpleMVIConfig
import com.arttttt.simplemvi.logging.LoggingPlugin
import com.arttttt.simplemvi.plugin.StorePlugin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/**
 * Creates a new [Store] with the given parameters.
 *
 * When [name] is not `null` and a logger is configured via `configureSimpleMVI`, a [LoggingPlugin]
 * is prepended to the [plugins] list automatically.
 *
 * @param name the [Store] name used by [LoggingPlugin] and debugging. Pass `null` to opt out of
 *   the automatic logging plugin.
 * @param initialize if `true`, [Store.init] is called before this function returns.
 * @param coroutineContext context for the [Store]'s [CoroutineScope]. The default
 *   `Dispatchers.Main.immediate + Job()` gives the [Store] its own root [Job]; override the
 *   [Job] (e.g. with a [kotlinx.coroutines.SupervisorJob]) when you need different cancellation
 *   semantics.
 * @param initialState the initial [State].
 * @param initialIntents intents dispatched immediately after [Store.init].
 * @param plugins the [StorePlugin] chain, invoked in list order. The automatic [LoggingPlugin]
 *   (when added) runs first.
 * @param actor the [Actor] hosted by the [Store].
 *
 * @see Store
 * @see Actor
 * @see StorePlugin
 * @see LoggingPlugin
 */
public fun <Intent : Any, State : Any, SideEffect : Any> createStore(
    name: StoreName?,
    initialize: Boolean = true,
    coroutineContext: CoroutineContext = Dispatchers.Main.immediate + Job(),
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