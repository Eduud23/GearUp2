plugins {
    // Apply these plugins in the project-level build.gradle.kts file.
    id("com.android.application") version "8.7.0" apply false
    id("com.google.gms.google-services") version "4.3.15" apply false
}

buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.3.15")
    }
}
