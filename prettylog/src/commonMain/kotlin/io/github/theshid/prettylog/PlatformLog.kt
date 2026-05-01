package io.github.theshid.prettylog

/**
 * Single seam between the library's formatting layer and whatever the host
 * platform considers "logging output" (Logcat on Android, stdout on the JVM,
 * NSLog on iOS, …). Implementations live in the per-target source sets.
 *
 * Best-effort: a failure inside a platform logger must never propagate.
 */
internal expect fun platformLog(tag: String, message: String, level: LogLevel)
