plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.pab_inventaris"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.pab_inventaris"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Untuk ConstraintLayout (pembungkus utama)

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Untuk Material Design (CardView, Button, FAB, Toolbar)
    implementation("com.google.android.material:material:1.10.0")

    // Untuk RecyclerView (daftar)
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // INI YANG KEMUNGKINAN BESAR HILANG:
    // Untuk SwipeRefreshLayout (tarik untuk refresh)
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Dependensi untuk Otentikasi Google
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")

    // Image Loading Library (Coil)
    implementation("io.coil-kt:coil:2.4.0")
}