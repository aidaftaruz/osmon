plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "uz.aidaftar.osmon"
    compileSdk = 34

    defaultConfig {
        applicationId = "uz.aidaftar.osmon"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    // Doimiy kalit: yangi APK eskisining ustiga o'rnatiladi
    signingConfigs {
        getByName("debug") {
            storeFile = file("osmon.keystore")
            storePassword = "osmon123"
            keyAlias = "osmon"
            keyPassword = "osmon123"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}
