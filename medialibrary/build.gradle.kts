plugins {
    id("com.android.library")
    id("kotlin-android")
}

val platformVersion = rootProject.extra["minSdkVersion"].toString()

android {
    compileSdkVersion(rootProject.extra["compileSdkVersion"] as Int)
    buildToolsVersion(rootProject.extra["buildToolsVersion"] as String)
    namespace = "com.cgfay.media"

    defaultConfig {
        minSdk = platformVersion.toInt()
        targetSdk = rootProject.extra["targetSdkVersion"] as Int

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            cmake {
                cppFlags.addAll(listOf("-std=c++11", "-D__STDC_CONSTANT_MACROS"))
                arguments.addAll(
                    listOf(
                        "-DANDROID_PLATFORM_LEVEL=$platformVersion",
                        "-DANDROID_TOOLCHAIN=clang"
                    )
                )
            }
            ndk {
                abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a"))
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = rootProject.extra["composeVersion"] as String
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    sourceSets["main"].apply {
        jniLibs.srcDir("src/main/jniLibs")
        jni.srcDirs(emptyList<String>())
        resources.srcDir("src/main/shell")
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }

    packagingOptions {
        pickFirst("lib/arm64-v8a/libyuv.so")
        pickFirst("lib/armeabi-v7a/libyuv.so")
        pickFirst("lib/arm64-v8a/libffmpeg.so")
        pickFirst("lib/armeabi-v7a/libffmpeg.so")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = rootProject.extra["composeVersion"] as String
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":utilslibrary"))
    implementation(project(":filterlibrary"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${rootProject.extra["kotlinVersion"]}")
    implementation("androidx.appcompat:appcompat:${rootProject.extra["appcompatVersion"]}")
    implementation("androidx.constraintlayout:constraintlayout:${rootProject.extra["constraintLayoutVersion"]}")
    implementation("androidx.activity:activity-compose:${rootProject.extra["activityComposeVersion"]}")
    implementation("androidx.compose.ui:ui:${rootProject.extra["composeVersion"]}")
    implementation("androidx.compose.material:material:${rootProject.extra["composeVersion"]}")
    implementation("androidx.compose.ui:ui-tooling-preview:${rootProject.extra["composeVersion"]}")
    implementation("androidx.compose.runtime:runtime:${rootProject.extra["composeVersion"]}")
    debugImplementation("androidx.compose.ui:ui-tooling:${rootProject.extra["composeVersion"]}")
    testImplementation("junit:junit:${rootProject.extra["junitVersion"]}")
    androidTestImplementation("androidx.test.ext:junit:${rootProject.extra["androidXJunitVersion"]}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${rootProject.extra["espressoVersion"]}")
}
