package com.arttttt.simplemvi.logging.logger

/**
 * each supported platform must provide a logging function
 */
public expect fun logV(tag: String, message: String)