plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "dev.ishant.easyniddle.r8smoke"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.ishant.easyniddle.r8smoke"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
}

dependencies {
    implementation(project(":easyniddle"))
}
