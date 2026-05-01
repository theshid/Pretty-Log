package io.github.theshid.prettylog

internal actual fun platformLog(tag: String, message: String, level: LogLevel) {
    when (level) {
        LogLevel.Debug -> android.util.Log.d(tag, message)
        LogLevel.Info -> android.util.Log.i(tag, message)
        LogLevel.Warning -> android.util.Log.w(tag, message)
        LogLevel.Error -> android.util.Log.e(tag, message)
        LogLevel.Critical -> android.util.Log.wtf(tag, message)
    }
}
