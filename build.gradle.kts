// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath(libs.androidx.navigation.safe.args.gradle.plugin)
        classpath(libs.hilt.android.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false
    alias(libs.plugins.hilt.android) apply false
    id("com.google.devtools.ksp") version "1.9.0-1.0.13" apply false
    id("com.google.firebase.crashlytics") version "3.0.1" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.10" apply false
}