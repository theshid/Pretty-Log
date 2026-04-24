package io.github.theshid.prettylog

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * Box-drawing logger that prints structured, bordered logs with caller info,
 * thread name, and emoji-prefixed level labels. Designed for debug builds
 * where Logcat readability matters more than throughput.
 *
 * Features:
 * - Minimum level filtering (anything below [minLevel] is a no-op).
 * - Auto-detection and pretty-printing of JSON payloads.
 * - Emoji-coded level tags for fast visual scanning.
 * - Crash breadcrumb recording — last 50 entries can be dumped to disk from
 *   an UncaughtExceptionHandler for post-mortem analysis.
 *
 * The default tag is configurable, so `your-app-name` shows up in Logcat
 * rather than a library-branded one.
 */
class PrettyLoggingService(
    private val defaultTag: String = "App",
    private val minLevel: LogLevel = LogLevel.Debug,
    private val maxStackLines: Int = DEFAULT_MAX_STACK_LINES,
) : LoggingService {

    @OptIn(ExperimentalSerializationApi::class)
    private val jsonPretty = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
    }

    override fun log(message: String, tag: String?, level: LogLevel, error: Throwable?) {
        if (level.ordinal < minLevel.ordinal) return

        // Record to breadcrumb buffer first — even if Logcat gets overrun or
        // throttled, we still have the entry in memory for a crash dump.
        LogBreadcrumbs.record(level, tag, message)

        val logcatTag = tag ?: defaultTag
        val caller = resolveCallerInfo()
        val prettyMessage = tryPrettyPrintJson(message)

        val lines = buildList {
            add(TOP_BORDER)
            add("$LEFT_BORDER ${level.emoji} ${level.label}  $THIN_SEPARATOR  Thread: ${Thread.currentThread().name}")
            add(MIDDLE_BORDER)
            if (caller != null) {
                add("$LEFT_BORDER $ARROW .${caller.className}.${caller.methodName}(${caller.fileName}:${caller.lineNumber})")
                add(MIDDLE_BORDER)
            }
            prettyMessage.lines().forEach { line ->
                add("$LEFT_BORDER $line")
            }
            if (error != null) {
                add(MIDDLE_BORDER)
                add("$LEFT_BORDER ${error::class.simpleName}: ${error.message}")
                error.stackTraceToString().lines().take(maxStackLines).forEach { line ->
                    add("$LEFT_BORDER   $line")
                }
            }
            add(BOTTOM_BORDER)
        }
        lines.forEach { line -> platformLog(logcatTag, line, level) }
    }

    /**
     * If [message] looks like JSON (starts with `{` or `[`), parse and
     * re-serialize it with indentation. Silently falls back to the raw
     * string on any parse error, so malformed-but-JSON-looking payloads
     * still print.
     */
    private fun tryPrettyPrintJson(message: String): String {
        val trimmed = message.trim()
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) return message
        return try {
            val element = jsonPretty.parseToJsonElement(trimmed)
            jsonPretty.encodeToString(JsonElement.serializer(), element)
        } catch (_: Exception) {
            message
        }
    }

    companion object {
        const val DEFAULT_MAX_STACK_LINES = 15

        private const val TOP_LEFT_CORNER = '\u250C'
        private const val BOTTOM_LEFT_CORNER = '\u2514'
        private const val MIDDLE_CORNER = '\u251C'
        private const val LEFT_BORDER = '\u2502'
        private const val DOUBLE_LINE = "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
            "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
            "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
            "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
            "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500" +
            "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500"
        private const val SINGLE_LINE = "\u2504\u2504\u2504\u2504\u2504\u2504\u2504\u2504\u2504\u2504" +
            "\u2504\u2504\u2504\u2504\u2504\u2504\u2504\u2504\u2504\u2504" +
            "\u2504\u2504\u2504\u2504\u2504\u2504\u2504\u2504\u2504\u2504" +
            "\u2504\u2504\u2504\u2504\u2504\u2504\u2504\u2504\u2504\u2504" +
            "\u2504\u2504\u2504\u2504\u2504\u2504\u2504\u2504\u2504\u2504" +
            "\u2504\u2504\u2504\u2504\u2504\u2504\u2504\u2504\u2504\u2504"
        private val TOP_BORDER = "$TOP_LEFT_CORNER$DOUBLE_LINE"
        private val BOTTOM_BORDER = "$BOTTOM_LEFT_CORNER$DOUBLE_LINE"
        private val MIDDLE_BORDER = "$MIDDLE_CORNER$SINGLE_LINE"
        private const val THIN_SEPARATOR = "\u2502"
        private const val ARROW = "\u2192"
    }
}
