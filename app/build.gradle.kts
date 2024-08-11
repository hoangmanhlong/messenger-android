plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp")
    id("androidx.navigation.safeargs.kotlin")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
    id("com.google.firebase.crashlytics")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.10"
}

android {
    namespace = "com.android.kotlin.familymessagingapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.android.kotlin.familymessagingapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "geminiApiKey", "\"AIzaSyCGETdkV_4RyNV4mZDZGAauqBf-W6zVQlM\"")
        buildConfigField("String", "geminiModelName", "\"gemini-1.5-flash\"")
        buildConfigField("String", "agoraAppId", "\"41853202c4364057b9da170ac81148c7\"")
        buildConfigField(
            "String",
            "firebaseServerClientId",
            "\"767365318867-9rp0qde0tlfemb139fqe4gfa3ta09b76.apps.googleusercontent.com\""
        )
        buildConfigField("String", "serverUrl", "\"http://47.129.5.135:6688\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }

        debug {
            isDebuggable = true
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
        dataBinding = true
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    // Kotlin + Coroutines
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.emoji2.emojipicker)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    implementation(platform(libs.firebase.bom))
    //noinspection UseTomlInstead
    implementation("com.google.firebase:firebase-database")
    //noinspection UseTomlInstead
    implementation("com.google.firebase:firebase-auth")
    //noinspection UseTomlInstead
    implementation("com.google.firebase:firebase-storage")

    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging.ktx)

    //Dagger - Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation("androidx.hilt:hilt-work:1.2.0")

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.gson)

    // Google Sign In SDK
    implementation(libs.play.services.auth.v2050)

    // Check memory leak
//    debugImplementation(libs.leakcanary.android)

    implementation(libs.glide)

    implementation(libs.androidx.core.splashscreen)

    // CameraX core library using the camera2 implementation

    // The following line is optional, as the core library is included indirectly by camera-camera2
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    // If you want to additionally use the CameraX Lifecycle library
    implementation(libs.androidx.camera.lifecycle)
    // If you want to additionally use the CameraX VideoCapture library
    implementation(libs.androidx.camera.video)
    // If you want to additionally use the CameraX View class
    implementation(libs.androidx.camera.view)
    // If you want to additionally add CameraX ML Kit Vision Integration
    implementation(libs.androidx.camera.mlkit.vision)
    // If you want to additionally use the CameraX Extensions library
    implementation(libs.androidx.camera.extensions)

    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

//    // To use Kotlin annotation processing tool (kapt)
//    kapt("androidx.room:room-compiler:$room_version")
    // To use Kotlin Symbol Processing (KSP)
    ksp(libs.androidx.room.compiler)

    // optional - Test helpers
    testImplementation(libs.androidx.room.testing)

    // optional - Paging 3 Integration
    implementation(libs.androidx.room.paging)

    implementation("com.facebook.android:facebook-android-sdk:[8,9)")


    implementation(libs.retrofit)
    // Retrofit with Gson Converter
    implementation(libs.converter.gson)
    // Retrofit with Kotlin serialization Converter
    implementation(libs.okhttp)
    // Kotlin serialization
    implementation(libs.kotlinx.serialization.json)

    debugImplementation("com.github.chuckerteam.chucker:library:4.0.0")
    releaseImplementation("com.github.chuckerteam.chucker:library-no-op:4.0.0")

    implementation("com.intuit.ssp:ssp-android:1.1.1")
    implementation("com.intuit.sdp:sdp-android:1.1.1")

    implementation("com.google.ai.client.generativeai:generativeai:0.7.0")

//    implementation("com.github.AgoraIO-Community:VideoUIKit-Android:v4.0.1")
    implementation("org.greenrobot:eventbus:3.3.1")

    implementation("io.socket:socket.io-client:2.1.1") {
        // excluding org.json which is provided by Android
        exclude(group = "org.json", module = "json")
    }

    implementation("com.google.mlkit:barcode-scanning:17.0.2")

    implementation("com.google.zxing:core:3.5.0")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

}

kapt {
    correctErrorTypes = true
}