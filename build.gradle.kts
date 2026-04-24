// Top-level build file. Plugins declared with `apply false` so subprojects
// opt in via their own build.gradle.kts. Versions kept here to keep them
// aligned across modules.
plugins {
    id("com.android.library") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "1.9.25" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.25" apply false
    id("maven-publish")
}
