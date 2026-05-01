package io.github.theshid.prettylog

import platform.Foundation.NSLog

/**
 * iOS actual — routes to NSLog so entries surface in Console.app and Xcode's
 * console pane the same way native Swift logs do.
 */
internal actual fun platformLog(tag: String, message: String, level: LogLevel) {
    NSLog("[${level.label}] $tag: $message")
}
