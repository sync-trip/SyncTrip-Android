import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.synctrip"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.synctrip"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }

        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { localProperties.load(it) }
        }
        buildConfigField("String", "KAKAO_NATIVE_KEY", "\"${localProperties["KAKAO_NATIVE_KEY"]}\"")
        buildConfigField("String", "KAKAO_REST_KEY", "\"${localProperties["KAKAO_REST_KEY"]}\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${localProperties["GOOGLE_WEB_CLIENT_ID"]}\"")
        manifestPlaceholders["KAKAO_NATIVE_KEY"] = localProperties["KAKAO_NATIVE_KEY"] ?: ""
        manifestPlaceholders["KAKAO_SCHEME"] = "kakao${localProperties["KAKAO_NATIVE_KEY"]}"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("com.kakao.sdk:v2-user:2.20.1")
    implementation("com.kakao.maps.open:android:2.12.8")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.google.android.material:material:1.11.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
}