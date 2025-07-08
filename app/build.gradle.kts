plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "2.0.21"
    id("com.google.devtools.ksp") version "2.0.21-1.0.27"
}

android {
    namespace = "com.example.pulseplayer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.pulseplayer"
        minSdk = 24
        targetSdk = 35
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
        compose = true
    }
}
val navVersion = "2.8.9"
val roomVersion = "2.6.0"
val exoPlayer = "1.3.1"
dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.lifecycle.process)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Material 3 (debes tener esto si usas Scaffold y TopAppBar)
    implementation("androidx.compose.material3:material3:1.2.1")

    // Material Icons
    implementation("androidx.compose.material:material-icons-extended:1.6.1")

    // Jetpack Compose Navigation
    implementation("androidx.navigation:navigation-compose:$navVersion")

    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Room
    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")

    // ExoPlayer (media3)
    implementation ("androidx.media3:media3-exoplayer:$exoPlayer")
    implementation ("androidx.media3:media3-ui:$exoPlayer")
    implementation ("androidx.media3:media3-session:$exoPlayer")
    // Coil para carátulas (opcional)
    implementation ("io.coil-kt:coil-compose:2.5.0")

    //Reoordenable
    implementation("org.burnoutcrew.composereorderable:reorderable:0.9.6")
    implementation ("androidx.media:media:1.6.0")

}