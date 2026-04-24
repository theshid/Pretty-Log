# Consumer ProGuard rules for PrettyLog.
# The library reads stack frames at runtime to resolve caller info. Keep the
# caller-resolution helper's signature so R8 doesn't strip it in release
# builds of consumer apps.
-keep class io.github.theshid.prettylog.CallerInfoKt { *; }
-keepattributes SourceFile,LineNumberTable
