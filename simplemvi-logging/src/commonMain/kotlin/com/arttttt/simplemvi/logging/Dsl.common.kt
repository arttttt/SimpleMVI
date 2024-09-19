package com.arttttt.simplemvi.logging

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.logging.logger.Logger

public fun <Intent : Any, State : Any, SideEffect : Any> loggingActor(
    name: String,
    logger: Logger,
    delegate: Actor<Intent, State, SideEffect>,
): Actor<Intent, State, SideEffect> {
    return LoggingActor(
        name = name,
        logger = logger,
        delegate = delegate,
    )
}