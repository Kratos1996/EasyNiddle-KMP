plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    jvm("desktop")

    sourceSets {
        getByName("desktopMain").dependencies {
            implementation(project(":easyniddle"))
            // The sample uses Navigation 3's entry DSL directly; the library keeps
            // its Navigation 3 implementation dependency encapsulated.
            implementation(libs.navigation3.ui)
            implementation(compose.desktop.currentOs)
            // Material 3 is versioned independently from the Compose Multiplatform plugin.
            implementation("org.jetbrains.compose.material3:material3:1.10.0-alpha05")
            implementation("org.jetbrains.compose.foundation:foundation:1.10.3")
            implementation("org.jetbrains.compose.ui:ui:1.10.3")
            implementation("org.jetbrains.compose.runtime:runtime:1.10.3")
        }
    }
}

compose.desktop {
    application {
        mainClass = "dev.ishant.easyniddle.sample.MainKt"
    }
}
