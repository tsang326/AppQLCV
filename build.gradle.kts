// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        // Make sure that you have the following two repositories
        google()  // Google's Maven repository
        mavenCentral()  // Maven Central repository

    }
    dependencies {
        // Add the dependency for the Google services Gradle plugin
        classpath("com.android.tools.build:gradle:7.0.3")

    }
}
plugins {
    id("com.android.application") version "8.1.4" apply false
    id("com.android.library") version "7.4.2" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
}