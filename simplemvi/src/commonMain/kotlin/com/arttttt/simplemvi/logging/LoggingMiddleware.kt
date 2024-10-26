package com.arttttt.simplemvi.logging

import com.arttttt.simplemvi.logging.logger.Logger
import com.arttttt.simplemvi.middleware.Middleware

public class LoggingMiddleware<Intent : Any, State : Any, SideEffect : Any>(
    private val name: String?,
    private val logger: Logger,
) : Middleware<Intent, State, SideEffect> {

    public companion object {

        private const val DEFAULT_STORE_NAME = "UnnamedStore"
    }

    override fun onInit(state: State) {
        logger.log(
            buildMessage(
                tag = name,
                message = "Initialization",
            )
        )
    }

    override fun onIntent(intent: Intent, state: State) {
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

    override fun onSideEffect(sideEffect: SideEffect, state: State) {
        logger.log(
            buildMessage(
                tag = name,
                event = "SideEffect",
                message = "$sideEffect",
            )
        )
    }

    override fun onDestroy(state: State) {
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