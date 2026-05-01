package io.github.theshid.prettylog

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Write the current breadcrumb buffer to
 * `filesDir/crash_logs/breadcrumbs_<timestamp>.txt`. Returns the file on
 * success, or `null` if the buffer was empty or the write failed (we
 * swallow exceptions so crash-path code can stay simple).
 *
 * Lives in androidMain because it depends on `Context.filesDir`. JVM
 * consumers that want a directory-based dump can copy this 15-line shape
 * into their own code; iOS consumers should mirror the pattern with
 * `NSDocumentDirectory`. The breadcrumb data itself comes from
 * [LogBreadcrumbs.getEntries], which is fully portable.
 */
fun LogBreadcrumbs.dumpToFile(context: Context): File? {
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
                entries.forEach { entry -> appendLine(entry.formatted()) }
            },
        )
        file
    } catch (_: Exception) {
        null
    }
}
