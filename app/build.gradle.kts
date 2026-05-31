plugins {
    id("com.android.application")
}

android {
    namespace = "com.xtc.dial.jianianhua"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.xtc.dial.jianianhua"
        minSdk = 25
        targetSdk = 28
        versionCode = 1
        versionName = "1.0.00.Local_J0_Ge136d466"
    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_PATH") ?: "E:/android.keystore"
            val keystoreFile = file(keystorePath)
            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
                keyAlias = System.getenv("KEY_ALIAS") ?: ""
                keyPassword = System.getenv("KEY_PASSWORD") ?: ""
            }
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    lint {
        disable.add("ExpiredTargetSdkVersion")
    }
}

dependencies {
    // XTC framework stubs (implementation — must be bundled in APK dex,
    // the device Launcher does NOT provide these classes at runtime)
    implementation(project(":sdk"))

    // Libraries bundled in the original APK (needed by framework at runtime)
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.google.protobuf:protobuf-java:3.25.0")
}

// Task to rename release APK to .cl (XTC dial format)
tasks.register("renameReleaseToCl") {
    dependsOn("assembleRelease")
    doLast {
        val apkDir = layout.buildDirectory.dir("outputs/apk/release").get().asFile
        val apk = apkDir.listFiles { f -> f.name.endsWith(".apk") }?.firstOrNull()
        if (apk != null) {
            val clFile = File(apk.parentFile, "${rootProject.name}.cl")
            apk.copyTo(clFile, overwrite = true)
            println("✓ .cl output: ${clFile.absolutePath} (${clFile.length() / 1024}KB)")
        }
    }
}
