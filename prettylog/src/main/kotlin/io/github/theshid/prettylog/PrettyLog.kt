package io.github.theshid.prettylog

/**
 * Entry point for configuring the library. Call [init] exactly once — ideally
 * from your `Application.onCreate`. After that the [Log] facade picks up the
 * service you configured.
 *
 * ```kotlin
 * class MyApp : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *         PrettyLog.init(
 *             isDebug = BuildConfig.DEBUG,
 *             defaultTag = "MyApp",
 *             minLevel = LogLevel.Debug,
 *         )
 *     }
 * }
 * ```
 *
 * If you never call [init], the library falls back to a zero-config
 * [PrettyLoggingService] so logging works out of the box for quick tries
 * — you just won't get your own app's tag in Logcat.
 */
object PrettyLog {

    @Volatile
    private var service: LoggingService? = null

    /**
     * Configure the library. Subsequent calls replace the active service —
     * useful for tests, not recommended in production.
     *
     * @param isDebug if true, installs [PrettyLoggingService] with bordered
     *   output. If false, installs [DefaultLoggingService] which is a plain
     *   pass-through to `android.util.Log` — cheaper and less noisy for
     *   release builds.
     * @param defaultTag tag used when a log call doesn't specify one.
     * @param minLevel entries below this level are dropped entirely by the
     *   pretty service. Has no effect on [DefaultLoggingService].
     * @param crashDumpHeader header written at the top of breadcrumb dump
     *   files. Defaults to "<defaultTag> Log Breadcrumbs".
     * @param custom if you want to pipe logs somewhere else (Crashlytics,
     *   Sentry, a file sink), pass your own [LoggingService] — it overrides
     *   the pretty/default choice.
     */
    fun init(
        isDebug: Boolean,
        defaultTag: String = "App",
        minLevel: LogLevel = LogLevel.Debug,
        crashDumpHeader: String = "$defaultTag Log Breadcrumbs",
        custom: LoggingService? = null,
    ) {
        LogBreadcrumbs.dumpHeader = crashDumpHeader
        service = custom ?: if (isDebug) {
            PrettyLoggingService(defaultTag = defaultTag, minLevel = minLevel)
        } else {
            DefaultLoggingService(defaultTag = defaultTag)
        }
    }

    internal fun requireService(): LoggingService =
        service ?: PrettyLoggingService().also { service = it }
}
