plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    jvm("desktop")

    sourceSets {
        desktopMain.dependencies {
            implementation(project(":easyniddle"))
            implementation(compose.desktop.currentOs)
            implementation("org.jetbrains.compose.material3:material3:1.10.3")
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
