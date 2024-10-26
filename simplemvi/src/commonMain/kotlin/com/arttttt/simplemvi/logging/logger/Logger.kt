package com.arttttt.simplemvi.logging.logger

/**
 * Logger that is used inside the [LoggingActor]
 */
public fun interface Logger {

    /**
     * This function prints the message
     */
    public fun log(message: String)
}