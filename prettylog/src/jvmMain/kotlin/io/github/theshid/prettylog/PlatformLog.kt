package io.github.theshid.prettylog

/**
 * Plain-stdout actual for the JVM target — desktop apps, server processes,
 * and the JVM-side of any KMP unit-test runner. Errors and warnings get
 * `System.err` so log readers can split streams.
 */
internal actual fun platformLog(tag: String, message: String, level: LogLevel) {
    val line = "[${level.label}] $tag: $message"
    when (level) {
        LogLevel.Error, LogLevel.Critical -> System.err.println(line)
        else -> println(line)
    }
}
