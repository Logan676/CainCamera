plugins {
    id("com.android.library")
}

android {
    compileSdk = project.rootProject.extra["compileSdkVersion"] as Int
    buildToolsVersion = project.rootProject.extra["buildToolsVersion"] as String
    namespace = "com.cgfay.landmarklibrary"

    defaultConfig {
        minSdk = project.rootProject.extra["minSdkVersion"] as Int
        targetSdk = project.rootProject.extra["targetSdkVersion"] as Int

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation("androidx.appcompat:appcompat:${project.rootProject.extra["appcompatVersion"]}")
    testImplementation("junit:junit:${project.rootProject.extra["junitVersion"]}")
    androidTestImplementation("androidx.test.ext:junit:${project.rootProject.extra["androidXJunitVersion"]}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${project.rootProject.extra["espressoVersion"]}")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${project.rootProject.extra["composeVersion"]}")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
