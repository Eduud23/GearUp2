plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.gearup"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.gearup"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.swiperefreshlayout)
    implementation(libs.cardview)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.firebase.messaging)
    implementation(libs.play.services.maps)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
    implementation ("androidx.viewpager2:viewpager2:1.0.0")
    implementation ("com.stripe:stripe-java:22.2.0")
    implementation ("com.stripe:stripe-android:17.2.0")
    implementation ("com.android.volley:volley:1.2.1")


    implementation ("com.google.firebase:firebase-database:20.3.0")
    implementation ("com.google.firebase:firebase-auth:22.1.2")


    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation ("org.tensorflow:tensorflow-lite:2.10.0")
    implementation ("org.tensorflow:tensorflow-lite-gpu:2.12.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.1")

    implementation ("org.apache.commons:commons-text:1.9")

    implementation ("com.google.android.gms:play-services-maps:18.1.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.google.maps.android:android-maps-utils:2.3.0")

    implementation(platform("com.google.firebase:firebase-bom:32.5.0"))

    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.2")

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")




}