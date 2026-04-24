package io.github.theshid.prettylog

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedDeque

/**
 * Circular buffer of the last [MAX_ENTRIES] log entries. Populated
 * automatically by [PrettyLoggingService] on every call. On crash (or on
 * demand), [dumpToFile] flushes the buffer to a timestamped text file under
 * the app's `filesDir/crash_logs/` for later inspection.
 *
 * Typical wiring:
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

    private const val MAX_ENTRIES = 50
    private val buffer = ConcurrentLinkedDeque<BreadcrumbEntry>()
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

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
            val time = timeFormat.format(Date(timestamp))
            val prefix = "${level.emoji} ${level.label}"
            val tagStr = tag?.let { "[$it] " } ?: ""
            return "$time  $prefix  $tagStr$message  (thread: $threadName)"
        }
    }

    fun record(level: LogLevel, tag: String?, message: String) {
        val entry = BreadcrumbEntry(
            timestamp = System.currentTimeMillis(),
            level = level,
            tag = tag,
            message = message,
            threadName = Thread.currentThread().name,
        )
        buffer.addLast(entry)
        while (buffer.size > MAX_ENTRIES) {
            buffer.pollFirst()
        }
    }

    fun getEntries(): List<BreadcrumbEntry> = buffer.toList()

    /**
     * Write the current buffer to `filesDir/crash_logs/breadcrumbs_<timestamp>.txt`.
     * Returns the file on success, or `null` if the buffer was empty or the
     * write failed (we swallow exceptions so crash-path code can stay simple).
     */
    fun dumpToFile(context: Context): File? {
        return try {
            val entries = getEntries()
            if (entries.isEmpty()) return null

            val dir = File(context.filesDir, "crash_logs")
            dir.mkdirs()
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
            val file = File(dir, "breadcrumbs_$timestamp.txt")

            file.writeText(
                buildString {
                    appendLine("=== $dumpHeader ===")
                    appendLine("Dumped at: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())}")
                    appendLine("Entries: ${entries.size}")
                    appendLine()
                    entries.forEach { entry ->
                        appendLine(entry.formatted())
                    }
                },
            )
            file
        } catch (_: Exception) {
            null
        }
    }

    fun clear() {
        buffer.clear()
    }
}
