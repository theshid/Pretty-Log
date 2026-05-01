package io.github.theshid.prettylog

/**
 * Minimal implementation that forwards to `android.util.Log` at the matching
 * level with no formatting. Intended for release builds where the bordered
 * pretty output is overkill and Logcat is probably being consumed by a
 * crash-reporting SDK that wants raw text.
 */
class DefaultLoggingService(
    private val defaultTag: String = "App",
) : LoggingService {
    override fun log(message: String, tag: String?, level: LogLevel, error: Throwable?) {
        platformLog(tag ?: defaultTag, message, level)
        if (error != null) {
            platformLog(tag ?: defaultTag, error.stackTraceToString(), level)
        }
    }
}
