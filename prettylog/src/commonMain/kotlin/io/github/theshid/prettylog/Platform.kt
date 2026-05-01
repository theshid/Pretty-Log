package io.github.theshid.prettylog

/**
 * Tiny portability seam for the bits of `java.lang.Thread` and
 * `java.text.SimpleDateFormat` that the formatter and breadcrumb buffer
 * touch. Per-target `actual`s live alongside [platformLog] in their
 * respective source sets.
 */
internal expect fun currentTimeMs(): Long

internal expect fun currentThreadName(): String

/** `HH:mm:ss.SSS` in the platform's default locale — used in breadcrumb dumps. */
internal expect fun formatLogTimestamp(timestampMs: Long): String
