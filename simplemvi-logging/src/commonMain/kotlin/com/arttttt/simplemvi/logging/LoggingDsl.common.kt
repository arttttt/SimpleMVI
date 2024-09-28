package com.arttttt.simplemvi.logging

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.logging.logger.DefaultLogger
import com.arttttt.simplemvi.logging.logger.Logger

/**
 * Creates a new [LoggingActor]
 *
 * @param name the name of the store in logs
 * @param logger an object responsible for printing logs
 * @param delegate the actual [Actor] to which the work is delegated
 *
 * @see LoggingActor
 */
public fun <Intent : Any, State : Any, SideEffect : Any> loggingActor(
    name: String?,
    logger: Logger = DefaultLogger,
    delegate: Actor<Intent, State, SideEffect>,
): Actor<Intent, State, SideEffect> {
    return LoggingActor(
        name = name,
        logger = logger,
        delegate = delegate,
    )
}