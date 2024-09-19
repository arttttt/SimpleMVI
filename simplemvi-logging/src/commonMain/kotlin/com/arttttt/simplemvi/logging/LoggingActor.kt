package com.arttttt.simplemvi.logging

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.logging.logger.Logger

public class LoggingActor<Intent : Any, State : Any, SideEffect : Any>(
    private val name: String,
    private val logger: Logger,
    private val delegate: Actor<Intent, State, SideEffect>,
) : Actor<Intent, State, SideEffect> {

    override fun init(
        getState: () -> State,
        reduce: ((State) -> State) -> Unit,
        onNewIntent: (intent: Intent) -> Unit,
        postSideEffect: (sideEffect: SideEffect) -> Unit
    ) {
        logger.log(
            buildMessage(
                tag = name,
                message = "Initialization",
            )
        )

        delegate.init(
            getState = getState,
            reduce = { updateState ->
                logger.log(
                    buildMessage(
                        tag = name,
                        event = "Old state",
                        message = "${getState()}"
                    )
                )

                reduce {
                    val newState = updateState(getState())

                    logger.log(
                        buildMessage(
                            tag = name,
                            event = "New state",
                            message = "$newState"
                        )
                    )

                    newState
                }
            },
            onNewIntent = onNewIntent,
            postSideEffect = { sideEffect ->
                logger.log(
                    buildMessage(
                        tag = name,
                        event = "SideEffect",
                        message = "$sideEffect",
                    )
                )

                postSideEffect(sideEffect)
            },
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

        delegate.onIntent(intent)
    }

    override fun destroy() {
        logger.log(
            buildMessage(
                tag = name,
                message = "Destroying",
            )
        )

        delegate.destroy()
    }

    private fun buildMessage(
        tag: String,
        event: String? = null,
        message: String,
    ): String {
        return buildString {
            append(tag)

            if (event != null) {
                append(" | ")
                append(event)
            }

            append(" | ")
            append(message)
        }
    }
}