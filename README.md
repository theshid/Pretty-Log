# PrettyLog

A structured, bordered, emoji-coded Kotlin logger for Android. Built for
debug readability in Logcat — with JSON auto-formatting, caller resolution,
performance timing, network-trip helpers, and a crash breadcrumb buffer.

```
┌────────────────────────────────────────────────────────────────
│ 🐛 DEBUG  │  Thread: main
├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄
│ → .HomeViewModel.loadHomeData(HomeViewModel.kt:64)
├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄
│ User loaded: {
│   "id": 123,
│   "name": "rachid"
│ }
└────────────────────────────────────────────────────────────────
```

## Installing

### Via JitPack (recommended for first-time publishers)

In your consumer project's **`settings.gradle.kts`** (or root `build.gradle`
repositories block), add JitPack:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Then in the consumer module's **`build.gradle.kts`**:

```kotlin
dependencies {
    implementation("com.github.theshid:Pretty-Log:0.1.0")
}
```

## Using

### 1. Initialize once

Call `PrettyLog.init(...)` in your `Application.onCreate`. That's all the
wiring there is.

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PrettyLog.init(
            isDebug = BuildConfig.DEBUG,
            defaultTag = "MyApp",
            minLevel = LogLevel.Debug,
        )
    }
}
```

- `isDebug = true` → installs `PrettyLoggingService` with the bordered output.
- `isDebug = false` → installs `DefaultLoggingService`, a plain pass-through
  to `android.util.Log` for release builds.
- Don't care? If you skip `init()` entirely, the facade lazily installs the
  pretty service with defaults so logging works out of the box.

### 2. Log things

```kotlin
import io.github.theshid.prettylog.Log

Log.d(message = "User loaded")
Log.d(tag = "Network", message = "Request sent")
Log.e(tag = "Auth", message = "Token expired", throwable = ex)
Log.wtf(message = "This should never happen")
```

Every entry gets:

- Emoji-coded level tag (🐛 / ℹ️ / ⚠️ / ❌ / 🔥).
- Auto-resolved caller: `ClassName.methodName(File.kt:line)`.
- Thread name.
- JSON payloads auto-indented — paste an API response into `Log.d`, it'll
  pretty-print.
- Truncated stack trace when a `Throwable` is attached.

### 3. Network trips

```kotlin
Log.network(
    method = "POST",
    url = "https://api.example.com/v1/users",
    status = 200,
    durationMs = 187,
    body = responseJson,
)
```

Level is chosen automatically — `Error` if an exception was passed, `Warning`
for 4xx/5xx, `Debug` otherwise.

### 4. Performance timing

```kotlin
val users = Log.timed("LoadUsers") { repository.fetchUsers() }
```

Logs at `Debug` (<1s) / `Info` (1–3s) / `Warning` (>3s). The returned value
is the block's result — drop-in wrapper, no restructuring.

### 5. Crash breadcrumbs

The last 50 log entries live in an in-memory circular buffer. On crash,
flush them to disk from an `UncaughtExceptionHandler`:

```kotlin
val previous = Thread.getDefaultUncaughtExceptionHandler()
Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
    try {
        Log.wtf(tag = "CRASH", message = "Uncaught on ${thread.name}: ${throwable.message}")
        LogBreadcrumbs.dumpToFile(applicationContext)
    } catch (_: Exception) { /* best-effort */ }
    previous?.uncaughtException(thread, throwable)
}
```

Files land at `filesDir/crash_logs/breadcrumbs_<timestamp>.txt`.

### 6. Custom sinks

Want Crashlytics / Sentry / a file sink? Implement `LoggingService` and pass
it via `init(custom = ...)`:

```kotlin
class CrashlyticsSink : LoggingService {
    override fun log(message: String, tag: String?, level: LogLevel, error: Throwable?) {
        FirebaseCrashlytics.getInstance().log("${tag ?: "App"}: $message")
        if (error != null) FirebaseCrashlytics.getInstance().recordException(error)
    }
}

PrettyLog.init(isDebug = false, custom = CrashlyticsSink())
```


## License

MIT.
