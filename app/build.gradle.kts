plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {

    namespace = "com.example.aubstudynotesapp"

    compileSdk = 36

    defaultConfig {

        applicationId = "com.example.aubstudynotesapp"

        minSdk = 24

        targetSdk = 36

        versionCode = 1

        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"AIzaSyB_Vimkmnh2ZvUZOPaEV_xhNW1ud0-Pj9E\""
        )
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {

        release {

            isMinifyEnabled = false
        }
    }

    compileOptions {

        sourceCompatibility =
            JavaVersion.VERSION_17

        targetCompatibility =
            JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")
    implementation(
        "androidx.appcompat:appcompat:1.6.1"
    )

    implementation(
        "com.google.android.material:material:1.11.0"
    )

    implementation(
        "androidx.constraintlayout:constraintlayout:2.1.4"
    )

    implementation(
        "androidx.recyclerview:recyclerview:1.3.2"
    )

    implementation(
        "androidx.activity:activity:1.10.1"
    )

    implementation(platform(
        "com.google.firebase:firebase-bom:34.13.0"
    ))

    implementation(
        "com.google.firebase:firebase-auth"
    )

    implementation(
        "com.google.firebase:firebase-firestore"
    )

    implementation(
        "com.google.firebase:firebase-storage"
    )

    implementation(
        "com.squareup.okhttp3:okhttp:4.12.0"
    )

    testImplementation(
        "junit:junit:4.13.2"
    )

    androidTestImplementation(
        "androidx.test.ext:junit:1.1.5"
    )

    androidTestImplementation(
        "androidx.test.espresso:espresso-core:3.5.1"
    )
}