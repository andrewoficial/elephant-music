import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform") version "2.1.0"
    id("org.jetbrains.compose") version "1.7.3"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0"
    id("com.android.application") version "8.9.1"
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm("desktop")

    sourceSets {
        val desktopMain by getting
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation("io.ktor:ktor-client-okhttp:3.0.1")
            implementation("com.googlecode.soundlibs:mp3spi:1.9.5.4")
            implementation("org.slf4j:slf4j-simple:2.0.13")
        }

        val androidMain by getting
        androidMain.dependencies {
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.runtime)
            implementation("androidx.activity:activity-compose:1.9.3")
            implementation("io.ktor:ktor-client-okhttp:3.0.1")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
            implementation("io.insert-koin:koin-core:4.0.0")
            implementation("io.insert-koin:koin-compose:4.0.0")
            implementation("io.ktor:ktor-client-core:3.0.1")
            implementation("io.ktor:ktor-client-content-negotiation:3.0.1")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.1")
        }
    }
}

android {
    namespace = "ru.kantser.elephantmusic"
    compileSdk = 36

    defaultConfig {
        applicationId = "ru.kantser.elephantmusic"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

compose.desktop {
    application {
        mainClass = (findProperty("mainClass") as String?) ?: "ru.kantser.elephantmusic.MainKt"
    }
}

compose.resources {
    packageOfResClass = "ru.kantser.elephantmusic.generated.resources"
    publicResClass = true
}
