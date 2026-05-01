pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // Kotlin Multiplatform's iOS targets need to add an Ivy repo for the
    // Konan compiler distribution. The KMP plugin contributes that repo at
    // project level, so we have to allow project repos to take precedence.
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PrettyLog"
include(":prettylog")
