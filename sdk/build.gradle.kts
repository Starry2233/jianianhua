plugins {
    id("com.android.library")
}

android {
    namespace = "com.xtc.dial"
    compileSdk = 34

    defaultConfig {
        minSdk = 25
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

// This is a stubs-only module — nothing to bundle
// The stubs are used at compile time and excluded from the final APK
