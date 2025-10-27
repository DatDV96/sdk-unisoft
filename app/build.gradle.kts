plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.jetbrains.serialization.android)
}

android {
    namespace = "com.ad.sample"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ad.sample"
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
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(project(":ads"))
    // Basic Android dependencies
    implementation ("androidx.core:core-ktx:1.7.0")
    implementation ("androidx.appcompat:appcompat:1.4.1")
    implementation ("com.google.android.material:material:1.5.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.3")


    // Navigation component
    implementation ("androidx.navigation:navigation-fragment-ktx:2.4.1")
    implementation ("androidx.navigation:navigation-ui-ktx:2.4.1")

    //admob
    implementation ("com.google.android.gms:play-services-ads:24.0.0")
    implementation ("com.google.android.gms:play-services-ads-identifier:18.1.0")

    implementation ("com.google.android.gms:play-services-appset:16.1.0")
    implementation ("com.google.android.gms:play-services-basement:18.4.0")
    //fb sdk
    implementation ("com.facebook.android:facebook-android-sdk:18.0.2")

    implementation ("com.airbnb.android:lottie:5.0.2")
    implementation ("com.squareup.retrofit2:retrofit:2.3.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:3.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.3.0")
    implementation ("com.squareup.retrofit2:adapter-rxjava2:2.4.0")
    implementation ("com.google.android.ump:user-messaging-platform:2.1.0")
    implementation ("com.facebook.shimmer:shimmer:0.5.0")

}