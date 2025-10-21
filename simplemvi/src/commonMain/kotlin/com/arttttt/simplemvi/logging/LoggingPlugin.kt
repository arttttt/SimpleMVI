package com.arttttt.simplemvi.logging

import com.arttttt.simplemvi.logging.logger.Logger
import com.arttttt.simplemvi.plugin.StorePlugin
import kotlin.properties.Delegates

public class LoggingPlugin<Intent : Any, State : Any, SideEffect : Any>(
    private val logger: Logger,
) : StorePlugin<Intent, State, SideEffect> {

    public companion object {

        private const val DEFAULT_STORE_NAME = "UnnamedStore"
    }

    private var context: StorePlugin.Context<Intent, State, SideEffect> by Delegates.notNull()

    private val name: String?
        get() = context.name?.name

    override fun onInit(context: StorePlugin.Context<Intent, State, SideEffect>) {
        this.context = context

        logger.log(
            buildMessage(
                tag = name,
                message = "Initialization",
            )
        )
    }

    override fun onIntent(intent: Intent) {
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