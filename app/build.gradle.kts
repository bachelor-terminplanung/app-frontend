plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "at.terminplaner"
    compileSdk = 35

    defaultConfig {
        applicationId = "at.terminplaner"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    val camerax_version = "1.2.0-rc01"
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")

    // the CameraX Extensions library
    implementation("androidx.camera:camera-extensions:${camerax_version}")

    // tessaact
    // https://stackoverflow.com/questions/7710123/how-can-i-use-tesseract-in-android
    implementation("com.rmtheis:tess-two:9.1.0")

    // Google Cloud Vision API
    implementation ("com.google.android.gms:play-services-vision:20.1.3")
    implementation ("com.google.mlkit:text-recognition:16.0.0-beta3")

    implementation ("com.squareup.okhttp3:okhttp:4.12.0")

    val nav_version = "2.7.7"

    implementation ("androidx.navigation:navigation-fragment:$nav_version")
    implementation ("androidx.navigation:navigation-ui:$nav_version")
}