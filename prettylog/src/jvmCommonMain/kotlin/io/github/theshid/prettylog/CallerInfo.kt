package io.github.theshid.prettylog

/**
 * Fully-qualified names of frames we always skip when walking up the stack
 * — the library's own classes and common plumbing. Kept as a constant so
 * consumers don't pay reflection cost on every log call.
 */
private val IGNORED_CLASSES =
    setOf(
        "io.github.theshid.prettylog.PrettyLoggingService",
        "io.github.theshid.prettylog.DefaultLoggingService",
        "io.github.theshid.prettylog.LoggingService",
        "io.github.theshid.prettylog.CallerInfoKt",
        "io.github.theshid.prettylog.Log",
        "io.github.theshid.prettylog.PrettyLog",
    )

private val IGNORED_PREFIXES =
    setOf(
        "java.lang.Thread",
        "jdk.internal.reflect.",
        "java.lang.reflect.",
        "sun.reflect.",
        "dalvik.system.VMStack",
        "kotlin.coroutines.",
        "kotlinx.coroutines.",
    )

actual fun resolveCallerInfo(): CallerInfo? {
    val stackTrace = Thread.currentThread().stackTrace
    val callerFrame =
        stackTrace.firstOrNull { element ->
            val name = element.className
            IGNORED_PREFIXES.none { prefix -> name.startsWith(prefix) } &&
                IGNORED_CLASSES.none { ignored ->
                    name == ignored || name.startsWith("$ignored\$")
                }
        } ?: return null

    return CallerInfo(
        fileName = callerFrame.fileName ?: "Unknown",
        lineNumber = callerFrame.lineNumber,
        methodName = callerFrame.methodName,
        className = callerFrame.className.substringAfterLast('.'),
    )
}
