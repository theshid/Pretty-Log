package io.github.theshid.prettylog

/**
 * Kotlin/Native doesn't ship a portable stack-trace introspection API that
 * matches the JVM's [Thread.currentThread().stackTrace] shape. The bordered
 * pretty output simply omits the call-site header on iOS — everything else
 * (level, message, thread name, stack-trace of an attached error) still works.
 */
actual fun resolveCallerInfo(): CallerInfo? = null
