plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "de.codereddev.wordup"
    compileSdk = 33

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ksp {
            arg("room.incremental", "true")
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    api("androidx.room:room-runtime:2.5.2")
    api("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    api("com.google.android.exoplayer:exoplayer-core:2.19.1")

    implementation("androidx.core:core-ktx:1.9.0")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    ksp("androidx.room:room-compiler:2.5.2")
}

// Publishing
// TODO: Must be migrated to kts
//ext {
//    PUBLISH_GROUP_ID = "de.codereddev.wordup"
//    PUBLISH_ARTIFACT_ID = "wordup-core"
//    PUBLISH_VERSION = "0.2.3"
//}

// TODO: Re-enable!apply from: "${rootProject.projectDir}/scripts/publish-mavencentral.gradle"
