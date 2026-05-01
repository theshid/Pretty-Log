package io.github.theshid.prettylog

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal actual fun currentTimeMs(): Long = System.currentTimeMillis()

internal actual fun currentThreadName(): String = Thread.currentThread().name

private val LOG_TIME_FORMAT = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

internal actual fun formatLogTimestamp(timestampMs: Long): String =
    synchronized(LOG_TIME_FORMAT) { LOG_TIME_FORMAT.format(Date(timestampMs)) }
