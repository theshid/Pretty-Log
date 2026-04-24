package io.github.theshid.prettylog

/**
 * Contract for anything that can consume a log event. The library ships two
 * implementations ([DefaultLoggingService] for release, [PrettyLoggingService]
 * for debug), but consumers are free to supply their own to pipe logs into
 * Crashlytics, Sentry, a custom file sink, etc.
 */
interface LoggingService {
    fun log(
        message: String,
        tag: String? = null,
        level: LogLevel = LogLevel.Debug,
        error: Throwable? = null,
    )
}
