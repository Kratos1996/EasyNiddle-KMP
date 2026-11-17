@file:OptIn(
    ExperimentalWasmDsl::class,
    ExperimentalAbiValidation::class,
)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.maven.publish)
}

group = "com.github.Kratos1996"
version = "0.1.0"

kotlin {
    // Track the published Kotlin API so accidental binary-breaking changes are caught in CI.
    abiValidation {
    }

    android {
        namespace = "dev.ishant.easyniddle"
        compileSdk = 37
        minSdk = 23
    }
    iosArm64()
    iosSimulatorArm64()
    jvm("desktop")
    wasmJs {
        browser {
            testTask {
                enabled = false
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.ui)
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

mavenPublishing {
    coordinates(
        groupId = "com.github.Kratos1996",
        artifactId = "easyniddle",
        version = "0.1.0",
    )

    pom {
        name.set("EasyNiddle")
        description.set("Type-safe navigation layer for Compose Multiplatform built on Navigation 3")
        url.set("https://github.com/Kratos1996/EasyNiddle-KMP")
        licenses {
            license {
                name.set("The Apache Software License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("Kratos1996")
                name.set("Ishant Sharma")
            }
        }
        scm {
            connection.set("scm:git:github.com/Kratos1996/EasyNiddle-KMP.git")
            developerConnection.set("scm:git:ssh://github.com/Kratos1996/EasyNiddle-KMP.git")
            url.set("https://github.com/Kratos1996/EasyNiddle-KMP")
        }
    }
}

gradle.projectsEvaluated {
    val wasmLock = layout.buildDirectory.file("wasm/package-lock.json").get().asFile
    wasmLock.parentFile.mkdirs()
    if (!wasmLock.exists()) {
        wasmLock.writeText("{}")
    }
    val jsLock = layout.buildDirectory.file("js/package-lock.json").get().asFile
    jsLock.parentFile.mkdirs()
    if (!jsLock.exists()) {
        jsLock.writeText("{}")
    }
}

tasks.withType<Test>().configureEach {
    failOnNoDiscoveredTests.set(false)
}

tasks.withType<org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest>().configureEach {
    failOnNoDiscoveredTests.set(false)
}
