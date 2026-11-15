plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
}

kotlin {
    android {
        namespace = "dev.ishant.easyniddle"
        compileSdk = 36
        minSdk = 23
    }
    iosArm64()
    iosSimulatorArm64()
    // Retained for Intel-based iOS simulator environments. This requires the
    // Compose Multiplatform 1.10.x dependency line (1.11.1+ dropped iosX64).
    iosX64()
    jvm("desktop")
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(libs.kotlinx.serialization.json)
            // Navigation 3 KMP's UI artifact brings its matching runtime transitively.
            implementation(libs.navigation3.ui)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.compose.ui:ui-test:1.10.3")
        }
    }
}
