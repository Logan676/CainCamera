// Top-level build file where you can add configuration options common to all sub-projects/modules.
apply(from = "versions.gradle.kts")

allprojects {
    repositories {
        google()
        mavenCentral()
        flatDir {
            dirs("libs")
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
