plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "de.codereddev.wordupexample"
    compileSdk = 36

    defaultConfig {
        applicationId = "de.codereddev.wordupexample"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = libs.versions.wordup.get()

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

    buildFeatures {
        compose = true
    }
}

dependencies {
    // TODO: Replace this with "de.codereddev.wordup:wordup-core:CURRENT_VERSION"
    implementation(project(":wordup-core"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Material Design (XML host theme)
    implementation(libs.material)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime.compose)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.hilt.android)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso)

    ksp(libs.hilt.compiler)
    ksp(libs.androidx.room.compiler)
}