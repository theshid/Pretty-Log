package io.github.theshid.prettylog

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSThread
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.localeWithLocaleIdentifier
import platform.Foundation.timeIntervalSince1970

internal actual fun currentTimeMs(): Long = (NSDate().timeIntervalSince1970 * 1000.0).toLong()

internal actual fun currentThreadName(): String = if (NSThread.isMainThread) "main" else NSThread.currentThread.name ?: "unknown"

private val logTimeFormatter: NSDateFormatter =
    NSDateFormatter().apply {
        locale = NSLocale.localeWithLocaleIdentifier("en_US_POSIX")
        dateFormat = "HH:mm:ss.SSS"
    }

internal actual fun formatLogTimestamp(timestampMs: Long): String {
    val date = NSDate.dateWithTimeIntervalSince1970(timestampMs / 1000.0)
    return logTimeFormatter.stringFromDate(date)
}
