plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "dev.ishant.easyniddle.r8smoke"
    compileSdk = 37

    defaultConfig {
        applicationId = "dev.ishant.easyniddle.r8smoke"
        minSdk = 24
        targetSdk = 37
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
