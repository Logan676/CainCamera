plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdk = rootProject.extra["compileSdkVersion"] as Int
    buildToolsVersion = rootProject.extra["buildToolsVersion"] as String
    namespace = "com.cgfay.cameralibrary"

    defaultConfig {
        minSdk = rootProject.extra["minSdkVersion"] as Int
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    repositories {
        flatDir {
            // \u4eba\u8138\u5173\u952e\u70b9\u68c0\u6d4b\u4f9d\u8d56\u5e93\u4e0b\u653e\u5230facedetectlibrary
            dirs(project(":facedetectlibrary").file("libs"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
        }
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = rootProject.extra["composeCompilerVersion"] as String
    }
    testOptions {
        targetSdk = rootProject.extra["targetSdkVersion"] as Int
    }
    lint {
        targetSdk = rootProject.extra["targetSdkVersion"] as Int
    }
}

val cameraxVersion = rootProject.extra["cameraXVersion"] as String
val lifecycleVersion = rootProject.extra["lifecycleVersion"] as String

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":facedetectlibrary"))
    implementation(project(":filterlibrary"))
    implementation(project(":gdxlibrary"))
    implementation(project(":imagelibrary"))
    implementation(project(":landmarklibrary"))
    implementation(project(":medialibrary"))
    implementation(project(":pickerlibrary"))
    implementation(project(":utilslibrary"))
    implementation(project(":videolibrary"))
    implementation(project(":widgetlibrary"))

    implementation("androidx.appcompat:appcompat:${rootProject.extra["appcompatVersion"]}")
    implementation("androidx.legacy:legacy-support-v4:${rootProject.extra["legacySupportVersion"]}")
    implementation("androidx.recyclerview:recyclerview:${rootProject.extra["recyclerViewVersion"]}")
    implementation("androidx.constraintlayout:constraintlayout:${rootProject.extra["constraintLayoutVersion"]}")
    implementation("com.google.android.material:material:${rootProject.extra["materialVersion"]}")

    implementation("androidx.lifecycle:lifecycle-runtime:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-extensions:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycleVersion")

    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")

    implementation("com.github.bumptech.glide:glide:${rootProject.extra["glideVersion"]}")

    val composeVersion = rootProject.extra["composeVersion"] as String
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation("androidx.activity:activity-compose:${rootProject.extra["activityComposeVersion"]}")
    implementation("io.coil-kt:coil-compose:${rootProject.extra["coilComposeVersion"]}")

    testImplementation("junit:junit:${rootProject.extra["junitVersion"]}")
    androidTestImplementation("androidx.test.ext:junit:${rootProject.extra["androidXJunitVersion"]}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${rootProject.extra["espressoVersion"]}")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${rootProject.extra["composeVersion"]}")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

