package io.github.theshid.prettylog

/**
 * Circular buffer of the last [MAX_ENTRIES] log entries. Populated
 * automatically by [PrettyLoggingService] on every call.
 *
 * The buffer is a plain [ArrayDeque] — KMP-portable. Logging from many
 * threads simultaneously can occasionally drop or duplicate a single
 * entry; that's an accepted trade-off for a debug breadcrumb log.
 *
 * Disk-dump APIs are platform-specific and live in per-target source sets
 * (`dumpToFile(Context)` on Android, available alongside the platform's
 * own filesystem conventions on JVM/iOS as future additions).
 *
 * Typical wiring (Android):
 *
 * ```kotlin
 * Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
 *     Log.wtf(tag = "CRASH", message = "Uncaught: ${throwable.message}")
 *     LogBreadcrumbs.dumpToFile(applicationContext)
 *     previousHandler.uncaughtException(thread, throwable)
 * }
 * ```
 */
object LogBreadcrumbs {
    internal const val MAX_ENTRIES = 50
    private val buffer = ArrayDeque<BreadcrumbEntry>(MAX_ENTRIES)

    /** Prefix for dumped crash-log files. Set via [PrettyLog.init]. */
    internal var dumpHeader: String = "Log Breadcrumbs"

    data class BreadcrumbEntry(
        val timestamp: Long,
        val level: LogLevel,
        val tag: String?,
        val message: String,
        val threadName: String,
    ) {
        fun formatted(): String {
            val time = formatLogTimestamp(timestamp)
            val prefix = "${level.emoji} ${level.label}"
            val tagStr = tag?.let { "[$it] " } ?: ""
            return "$time  $prefix  $tagStr$message  (thread: $threadName)"
        }
    }

    fun record(level: LogLevel, tag: String?, message: String) {
        val entry =
            BreadcrumbEntry(
                timestamp = currentTimeMs(),
                level = level,
                tag = tag,
                message = message,
                threadName = currentThreadName(),
            )
        buffer.addLast(entry)
        while (buffer.size > MAX_ENTRIES) {
            buffer.removeFirst()
        }
    }

    fun getEntries(): List<BreadcrumbEntry> = buffer.toList()

    fun clear() {
        buffer.clear()
    }
}
