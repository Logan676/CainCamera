plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdk = rootProject.extra["compileSdkVersion"] as Int
    buildToolsVersion = rootProject.extra["buildToolsVersion"] as String
    namespace = "com.cgfay.imagelibrary"

    defaultConfig {
        minSdk = rootProject.extra["minSdkVersion"] as Int
        targetSdk = rootProject.extra["targetSdkVersion"] as Int

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":utilslibrary"))
    implementation(project(":filterlibrary"))
    implementation("androidx.appcompat:appcompat:${rootProject.extra["appcompatVersion"]}")
    implementation("androidx.legacy:legacy-support-v4:${rootProject.extra["legacySupportVersion"]}")
    implementation("androidx.recyclerview:recyclerview:${rootProject.extra["recyclerViewVersion"]}")
    implementation("androidx.constraintlayout:constraintlayout:${rootProject.extra["constraintLayoutVersion"]}")
    implementation("androidx.activity:activity-compose:${rootProject.extra["activityComposeVersion"]}")
    implementation("androidx.compose.ui:ui:${rootProject.extra["composeVersion"]}")
    implementation("androidx.compose.material:material:${rootProject.extra["composeVersion"]}")
    implementation("androidx.compose.ui:ui-tooling-preview:${rootProject.extra["composeVersion"]}")
    debugImplementation("androidx.compose.ui:ui-tooling:${rootProject.extra["composeVersion"]}")
    testImplementation("junit:junit:${rootProject.extra["junitVersion"]}")
    androidTestImplementation("androidx.test.ext:junit:${rootProject.extra["androidXJunitVersion"]}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${rootProject.extra["espressoVersion"]}")
}
