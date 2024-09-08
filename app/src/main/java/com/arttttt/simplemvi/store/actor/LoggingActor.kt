package com.arttttt.simplemvi.store.actor

import com.arttttt.simplemvi.store.logger.Logger

class LoggingActor<in Intent, State, SideEffect>(
    private val name: String,
    private val logger: Logger,
    private val delegate: Actor<Intent, State, SideEffect>,
) : Actor<Intent, State, SideEffect> {

    override fun init(
        getState: () -> State,
        reduce: ((State) -> State) -> Unit,
        postSideEffect: (sideEffect: SideEffect) -> Unit
    ) {
        delegate.init(
            getState = getState,
            reduce = { updateState ->
                logger.log(
                    buildMessage(
                        tag = name,
                        message = "State before reduce(${getState()})"
                    )
                )

                reduce {
                    val newState = updateState(getState())

                    logger.log(
                        buildMessage(
                            tag = name,
                            message = "State after reduce($newState)"
                        )
                    )

                    newState
                }
            },
            postSideEffect = { sideEffect ->
                logger.log(
                    buildMessage(
                        tag = name,
                        message = "Side Effect($sideEffect)",
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
                message = "Intent($intent)",
            )
        )

        delegate.onIntent(intent)
    }

    override fun destroy() {
        delegate.destroy()
    }

    private fun buildMessage(
        tag: String,
        message: String,
    ): String {
        return buildString {
            append(tag)
            append(": ")
            append(message)
        }
    }
}