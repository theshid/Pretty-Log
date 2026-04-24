package io.github.theshid.prettylog

/**
 * Snapshot of the call site that triggered a log entry — rendered in the
 * header of each bordered log block so you can jump straight to the source.
 */
data class CallerInfo(
    val fileName: String,
    val lineNumber: Int,
    val methodName: String,
    val className: String,
)

/**
 * Fully-qualified names of frames we always skip when walking up the stack
 * — the library's own classes and common plumbing. Kept as a constant so
 * consumers don't pay reflection cost on every log call.
 */
private val IGNORED_CLASSES = setOf(
    "io.github.theshid.prettylog.PrettyLoggingService",
    "io.github.theshid.prettylog.DefaultLoggingService",
    "io.github.theshid.prettylog.LoggingService",
    "io.github.theshid.prettylog.CallerInfoKt",
    "io.github.theshid.prettylog.Log",
    "io.github.theshid.prettylog.PrettyLog",
)

private val IGNORED_PREFIXES = setOf(
    "java.lang.Thread",
    "jdk.internal.reflect.",
    "java.lang.reflect.",
    "sun.reflect.",
    "dalvik.system.VMStack",
    "kotlin.coroutines.",
    "kotlinx.coroutines.",
)

/**
 * Walks the current thread's stack and returns the first frame that doesn't
 * belong to the logger itself or to reflective/coroutine plumbing — i.e. the
 * user code that invoked `Log.d(...)`.
 *
 * Returns `null` if no suitable frame is found (shouldn't happen in practice).
 */
fun resolveCallerInfo(): CallerInfo? {
    val stackTrace = Thread.currentThread().stackTrace
    val callerFrame = stackTrace.firstOrNull { element ->
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
