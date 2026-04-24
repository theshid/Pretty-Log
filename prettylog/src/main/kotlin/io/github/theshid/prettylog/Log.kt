package io.github.theshid.prettylog

import kotlin.system.measureTimeMillis

/**
 * Static-style logging facade. Delegates to the [LoggingService] registered
 * via [PrettyLog.init]. Named `Log` so call sites read like the familiar
 * `android.util.Log` — but with structured output and richer helpers.
 *
 * Basic usage:
 * ```kotlin
 * Log.d(message = "User loaded")
 * Log.d(tag = "Network", message = "Request sent")
 * Log.e(tag = "Auth", message = "Token expired", throwable = ex)
 * Log.wtf(message = "This should never happen")
 * ```
 *
 * Network logging:
 * ```kotlin
 * Log.network(method = "POST", url = "https://api.example.com", status = 200, body = jsonResponse)
 * ```
 *
 * Performance timing:
 * ```kotlin
 * val result = Log.timed("ParseFeed") { parser.parse(body) }
 * ```
 */
object Log {

    @PublishedApi
    internal val service: LoggingService get() = PrettyLog.requireService()

    // ── Standard levels ──────────────────────────────────────

    fun d(tag: String? = null, message: String) =
        service.log(message = message, tag = tag, level = LogLevel.Debug)

    fun i(tag: String? = null, message: String) =
        service.log(message = message, tag = tag, level = LogLevel.Info)

    fun w(tag: String? = null, message: String) =
        service.log(message = message, tag = tag, level = LogLevel.Warning)

    fun e(tag: String? = null, message: String, throwable: Throwable? = null) =
        service.log(message = message, tag = tag, level = LogLevel.Error, error = throwable)

    fun wtf(tag: String? = null, message: String, throwable: Throwable? = null) =
        service.log(message = message, tag = tag, level = LogLevel.Critical, error = throwable)

    // ── Network request/response logger ──────────────────────

    /**
     * Structured log for a single HTTP round-trip. The level is auto-chosen:
     * `Error` if an exception was thrown, `Warning` for 4xx/5xx, else `Debug`.
     */
    fun network(
        method: String,
        url: String,
        status: Int? = null,
        headers: Map<String, String>? = null,
        body: String? = null,
        durationMs: Long? = null,
        error: Throwable? = null,
    ) {
        val level = when {
            error != null -> LogLevel.Error
            status != null && status >= 400 -> LogLevel.Warning
            else -> LogLevel.Debug
        }
        val message = buildString {
            append("$method $url")
            if (status != null) append("  →  $status")
            if (durationMs != null) append("  (${durationMs}ms)")
            if (headers != null && headers.isNotEmpty()) {
                appendLine()
                append("Headers:")
                headers.forEach { (k, v) -> appendLine(); append("  $k: $v") }
            }
            if (body != null) {
                appendLine()
                append("Body: $body")
            }
        }
        service.log(message = message, tag = "Network", level = level, error = error)
    }

    // ── Performance timing ───────────────────────────────────

    /**
     * Wrap a block and log how long it took. The level is auto-chosen so a
     * glance at Logcat flags slow paths: `Warning` if > 3s, `Info` if > 1s,
     * `Debug` otherwise.
     */
    inline fun <T> timed(label: String, tag: String? = null, block: () -> T): T {
        var result: T
        val elapsed = measureTimeMillis { result = block() }
        service.log(
            message = "\u23F1 $label completed in ${elapsed}ms",
            tag = tag ?: "Perf",
            level = when {
                elapsed > 3000 -> LogLevel.Warning
                elapsed > 1000 -> LogLevel.Info
                else -> LogLevel.Debug
            },
        )
        return result
    }
}
