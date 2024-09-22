package com.arttttt.simplemvi.logging.logger

public actual fun logV(tag: String, message: String) {
    println("$tag: $message")
}