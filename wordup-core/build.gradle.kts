plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("maven-publish")
    id("signing")
}

android {
    namespace = "de.codereddev.wordup"
    compileSdk = 34

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

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    api("androidx.room:room-runtime:2.6.1")
    api("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    api("androidx.media3:media3-exoplayer:1.2.0")

    implementation("androidx.core:core-ktx:1.12.0")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    ksp("androidx.room:room-compiler:2.6.1")
}

version = "0.2.4-SNAPSHOT"

configure<PublishingExtension> {
    publications {
        afterEvaluate {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = "de.codereddev.wordup"
                artifactId = "wordup-core"
                version = project.version as String

                pom {
                    packaging = "aar"
                    name.set("WordUp Core")
                    description.set("An Android open source framework for creating soundboard apps.")
                    licenses {
                        license {
                            name.set("The Unlicense")
                            url.set("https://unlicense.org/")
                        }
                    }
                    developers {
                        developer {
                            id.set("CodeRedDev")
                            name.set("Michael Martel")
                            email.set("codered.dev.germany@gmail.com")
                        }
                    }
                    scm {
                        url.set("https://github.com/CodeRedDev/WordUp/tree/master")
                        connection.set("scm:git://github.com/CodeRedDev/WordUp.git")
                        developerConnection.set("scm:git:ssh://github.com:CodeRedDev/WordUp.git")
                    }
                }
            }
        }
        repositories {
            if (version.toString().endsWith("SNAPSHOT")) {
                maven("https://oss.sonatype.org/content/repositories/snapshots/") {
                    name = "sonatypeSnapshotRepository"
                    credentials(PasswordCredentials::class)
                }
            } else {
                maven("https://oss.sonatype.org/service/local/staging/deploy/maven2/") {
                    name = "sonatypeReleaseRepository"
                    credentials(PasswordCredentials::class)
                }
            }
        }
    }
}

signing {
    // signing.keyId, signing.password and signing.secretKeyRingFile
    // must be put into global gradle.properties when signing

    // Since the publication itself was created in `afterEvaluate`, we must
    // do the same here.
    afterEvaluate {
        // This adds a signing stage to the publish task in-place (so we keep
        // using the same task name; it just also performs signing now).
        sign(publishing.publications["release"])
    }
}