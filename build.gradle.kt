// Top-level build file where you can add configuration options common to all sub-projects/modules.
apply from: "versions.gradle.kts"

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Updated to support compileSdk 35
        classpath 'com.android.tools.build:gradle:8.3.0'
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:${project.property('kotlinVersion')}"
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        flatDir {
            dirs 'libs'
        }
    }
}

tasks.register('clean', Delete) {
    delete rootProject.buildDir
}
