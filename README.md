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
    implementation("com.github.the-shid:PrettyLog:0.1.0")
}
```

(Replace `the-shid` with your GitHub username, and `0.1.0` with whatever
tag you published.)

### Via Maven Local (for iterating on the library itself)

From the PrettyLog root:

```bash
./gradlew :prettylog:publishToMavenLocal
```

In the consumer project's `settings.gradle.kts`, add `mavenLocal()` to the
repositories block, then:

```kotlin
dependencies {
    implementation("io.github.theshid:prettylog:0.1.0")
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

## Publishing your own build

You've got the code, it builds, you want others to be able to use it. Three
paths, easiest first.

### Option A — JitPack (zero config, works today)

1. Push the project to GitHub. Public repo is easiest — JitPack free tier
   works best with public.
2. Go to **GitHub → Releases → Draft a new release**. Tag it (e.g. `0.1.0`),
   title it, publish.
3. Visit `https://jitpack.io/#YOUR_USERNAME/PrettyLog` — JitPack picks up
   the tag automatically. First build takes a minute or two.
4. Consumers use `com.github.YOUR_USERNAME:PrettyLog:0.1.0`.

No signing, no credentials, no account setup beyond GitHub. Ideal for a
first library.

### Option B — GitHub Packages

Lives inside your GitHub org, good if you want finer-grained access control
(private consumers). Downside: consumers have to authenticate with a
Personal Access Token even for public packages — adds friction.

### Option C — Maven Central

The gold standard. Consumers pick it up with zero config. But requires:

- A groupId you can prove ownership of (typically a reversed domain).
- A Sonatype OSSRH account.
- PGP signing keys.
- A `staging repository` → `release` workflow.

Not recommended for a first library — use JitPack until you have a domain
and a reason.

## Renaming the package

If `io.github.theshid.prettylog` isn't what you want — maybe you have
your own domain, or a company namespace — rename in three places:

1. The package declaration at the top of each `.kt` file under
   `prettylog/src/main/kotlin/`.
2. The folder path itself (rename the `io/github/theshid/prettylog/`
   chain to match).
3. The `IGNORED_CLASSES` set in `CallerInfo.kt` (six FQNs to update).
4. The `namespace` in `prettylog/build.gradle.kts` and the `groupId` in the
   `publishing {}` block.

Android Studio's "Refactor → Rename" handles 1, 2, and 4 with one click;
step 3 is a manual find-and-replace because those are string literals, not
references.

## What's in the box

```
prettylog/src/main/kotlin/io/github/theshid/prettylog/
├── CallerInfo.kt             stack-frame → caller snapshot
├── DefaultLoggingService.kt  release-build pass-through
├── Log.kt                    the facade (Log.d / Log.network / Log.timed)
├── LogBreadcrumbs.kt         last-50 circular buffer + dump-to-file
├── LogLevel.kt               Debug / Info / Warning / Error / Critical
├── LoggingService.kt         the contract — implement for custom sinks
├── PlatformLog.kt            only place that touches android.util.Log
├── PrettyLog.kt              init / config holder
└── PrettyLoggingService.kt   the bordered, JSON-aware debug logger
```

## License

MIT.
