package com.arttttt.simplemvi.logging

import com.arttttt.simplemvi.logging.logger.Logger
import com.arttttt.simplemvi.plugin.Pipeline
import com.arttttt.simplemvi.plugin.StorePlugin
import kotlin.properties.Delegates

/**
 * A [StorePlugin] that logs every lifecycle event of the [com.arttttt.simplemvi.store.Store] —
 * init, intents, state changes, side effects, and destruction — via the supplied [Logger].
 *
 * Added automatically by `createStore` when a store [name] is provided and a logger is configured
 * via `configureSimpleMVI`. Can also be registered manually to log a store that opts out of the
 * automatic plugin (`name = null`) or to log with a custom [Logger].
 *
 * Each log line has the format `<tag> | <event> | <payload>`, where `<tag>` is [name] (or
 * `UnnamedStore` if `null`) and `<event>` identifies the lifecycle stage. The plugin never
 * modifies intents — [Pipeline.block] and [Pipeline.transform] are not called.
 *
 * @param name tag used at the start of every log line. `null` falls back to `UnnamedStore`.
 * @param logger sink that receives the formatted messages.
 */
public class LoggingPlugin<Intent : Any, State : Any, SideEffect : Any>(
    private val name: String?,
    private val logger: Logger,
) : StorePlugin<Intent, State, SideEffect> {

    public companion object {

        private const val DEFAULT_STORE_NAME = "UnnamedStore"
    }

    private var context: StorePlugin.Context<Intent, State, SideEffect> by Delegates.notNull()

    override fun onInit(context: StorePlugin.Context<Intent, State, SideEffect>) {
        this.context = context

        logger.log(
            buildMessage(
                tag = name,
                message = "Initialization",
            )
        )
    }

    override fun Pipeline<Intent>.onIntent(intent: Intent) {
        logger.log(
            buildMessage(
                tag = name,
                event = "Intent",
                message = "$intent",
            )
        )
    }

    override fun onStateChanged(oldState: State, newState: State) {
        logger.log(
            buildMessage(
                tag = name,
                event = "Old state",
                message = "$oldState"
            )
        )

        logger.log(
            buildMessage(
                tag = name,
                event = "New state",
                message = "$newState"
            )
        )
    }

    override fun onSideEffect(sideEffect: SideEffect) {
        logger.log(
            buildMessage(
                tag = name,
                event = "SideEffect",
                message = "$sideEffect",
            )
        )
    }

    override fun onDestroy() {
        logger.log(
            buildMessage(
                tag = name,
                message = "Destroying",
            )
        )
    }

    private fun buildMessage(
        tag: String?,
        event: String? = null,
        message: String,
    ): String {
        return buildString {
            append(tag ?: DEFAULT_STORE_NAME)

            if (event != null) {
                append(" | ")
                append(event)
            }

            append(" | ")
            append(message)
        }
    }
}