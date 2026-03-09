plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.iproov.example"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.iproov.example"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        debug {
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        languageVersion = JavaVersion.VERSION_1_8.toString()
        apiVersion = JavaVersion.VERSION_1_8.toString()
    }

    buildFeatures {
        viewBinding = true
    }
}

// This is needed for versions prior to 9.x.x - https://github.com/iProov/android/wiki/Known-Issues
configurations.configureEach {
    exclude(group = "org.json", module = "json")
}

dependencies {

    implementation(libs.androidX.appcompat)
    implementation(libs.android.constraint.layout)
    implementation(libs.android.lifecycle)

    implementation(libs.iproov.sdk)

    implementation(libs.kotlin.fuel) {
        exclude(group = "org.json", module = "json")
    }
}