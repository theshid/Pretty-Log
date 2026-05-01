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
 * Walks the current thread's stack and returns the first frame that doesn't
 * belong to the logger itself or to reflective/coroutine plumbing — i.e. the
 * user code that invoked `Log.d(...)`.
 *
 * Returns `null` when call-site resolution isn't supported (Native targets
 * without a portable stack-trace API), or when no suitable frame is found.
 */
expect fun resolveCallerInfo(): CallerInfo?
