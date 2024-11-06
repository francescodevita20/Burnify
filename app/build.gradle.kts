plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.burnify"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.burnify"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Dipendenze per Wear OS
    implementation("androidx.wear:wear:1.3.0") // Libreria Wear OS
    implementation(libs.compose.material.v100) // Compose per Wear OS
    implementation ("androidx.compose.material:material-icons-extended:1.7.5")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    // Librerie tramite `libs.`
    implementation(libs.play.services.wearable)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.compose.material)
    implementation(libs.compose.foundation)
    implementation(libs.wear.tooling.preview)
    implementation(libs.activity.compose)
    implementation(libs.core.splashscreen)
    implementation(libs.tiles)
    implementation(libs.tiles.material)
    implementation(libs.horologist.compose.tools)
    implementation(libs.horologist.tiles)
    implementation(libs.watchface.complications.data.source.ktx)
    implementation(libs.material3.android)
    implementation(libs.runtime.livedata)

    // Dipendenze per test Android
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)

    // Dipendenze di debug
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}
