rootProject.buildFileName = "build.gradle.kts"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "8.9.3"
        id("com.android.library")    version "8.9.3"
        id("org.jetbrains.kotlin.android")          version "2.0.0"
        id("org.jetbrains.kotlin.plugin.compose")   version "2.0.0"
    }
}

include(
    ":app",
    ":cameralibrary",
    ":facedetectlibrary",
    ":filterlibrary",
    ":videolibrary",
    ":gdxlibrary",
    ":widgetlibrary",
    ":landmarklibrary",
    ":pickerlibrary",
    ":imagelibrary",
    ":medialibrary",
    ":utilslibrary"
)
