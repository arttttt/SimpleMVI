package com.arttttt.simplemvi.logging.logger

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val logTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)

public actual fun logV(tag: String, message: String) {
    val time = ZonedDateTime.now().format(logTimeFormatter)
    println("$time [$tag]: $message")
}
