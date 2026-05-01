import org.gradle.internal.os.OperatingSystem

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.serialization")
    `maven-publish`
}

// iOS Kotlin/Native targets need a macOS host (Apple toolchain ships only on
// macOS). Linux CI runners — JitPack, GitHub Actions ubuntu-latest — silently
// skip iOS publication; macOS builds publish all five variants.
val isMac = OperatingSystem.current().isMacOsX

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions { jvmTarget = "17" }
        }
    }

    jvm {
        compilations.all {
            kotlinOptions { jvmTarget = "17" }
        }
    }

    if (isMac) {
        iosX64()
        iosArm64()
        iosSimulatorArm64()
    }

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
        }

        // Intermediate source set shared by androidMain + jvmMain — both run
        // on a JVM, so stack-walk (StackTraceElement) and date-formatting
        // (SimpleDateFormat) actuals live here once instead of twice.
        val jvmCommonMain by creating { dependsOn(commonMain.get()) }
        val androidMain by getting { dependsOn(jvmCommonMain) }
        val jvmMain by getting { dependsOn(jvmCommonMain) }

        if (isMac) {
            val iosX64Main by getting
            val iosArm64Main by getting
            val iosSimulatorArm64Main by getting
            val iosMain by creating {
                dependsOn(commonMain.get())
                iosX64Main.dependsOn(this)
                iosArm64Main.dependsOn(this)
                iosSimulatorArm64Main.dependsOn(this)
            }
        }
    }
}

android {
    namespace = "io.github.theshid.prettylog"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// KMP plugin auto-publishes one artifact per target plus a "metadata" root.
// Consumers depending on `io.github.theshid:prettylog:0.1.0` continue to work —
// Gradle picks the right variant via Gradle Module Metadata. JitPack supports
// KMP publication out of the box.
afterEvaluate {
    publishing {
        publications.withType<MavenPublication>().configureEach {
            groupId = "io.github.theshid"
            version = "0.2.0"
            pom {
                name.set("PrettyLog")
                description.set("Structured, bordered, emoji-coded Kotlin logger — KMP (Android, JVM, iOS).")
                url.set("https://github.com/the-shid/PrettyLog")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
            }
        }
    }
}
