package io.github.theshid.prettylog

/**
 * Severity classification for a log entry. Renamed from `MonitoringLogLevel`
 * to `LogLevel` now that this is a standalone library — callers don't need
 * the "Monitoring" prefix to disambiguate from anything else.
 *
 * Ordering matters: `Debug` < `Info` < `Warning` < `Error` < `Critical`.
 * The [PrettyLoggingService] uses [ordinal] comparisons to filter entries
 * below its configured minimum level.
 */
enum class LogLevel {
    Debug,
    Info,
    Warning,
    Error,
    Critical;

    /** One-char icon used in the bordered output and in breadcrumb dumps. */
    val emoji: String
        get() = when (this) {
            Debug -> "\uD83D\uDC1B"     // bug
            Info -> "\u2139\uFE0F"       // info
            Warning -> "\u26A0\uFE0F"    // warn
            Error -> "\u274C"            // x
            Critical -> "\uD83D\uDD25"   // fire
        }

    /** Uppercase short label used next to the emoji. */
    val label: String
        get() = when (this) {
            Debug -> "DEBUG"
            Info -> "INFO"
            Warning -> "WARN"
            Error -> "ERROR"
            Critical -> "CRITICAL"
        }
}
