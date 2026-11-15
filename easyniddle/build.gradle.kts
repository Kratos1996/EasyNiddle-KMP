@file:OptIn(
    org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class,
    org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class,
)

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
}

kotlin {
    // Track the published Kotlin API so accidental binary-breaking changes are caught in CI.
    abiValidation {
        enabled.set(true)
    }

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
            implementation("org.jetbrains.compose.runtime:runtime:1.10.3")
            implementation("org.jetbrains.compose.ui:ui:1.10.3")
            implementation(libs.kotlinx.serialization.json)
            // NiddleRoute publicly extends Navigation 3's NavKey, so consumers
            // must receive the Navigation 3 runtime on their compile classpath.
            api(libs.navigation3.ui)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
