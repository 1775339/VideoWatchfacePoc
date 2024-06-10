
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
}

android {
    namespace = "com.titan.titanvideotrimmingpoc"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.titan.titanvideotrimmingpoc"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {

        viewBinding = true

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
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("androidx.media3:media3-exoplayer:1.3.1")
    implementation ("androidx.media3:media3-ui:1.3.1")
    implementation ("androidx.media3:media3-common:1.3.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    // For control over item selection of both touch and mouse driven selection
    implementation("androidx.recyclerview:recyclerview-selection:1.1.0")

    implementation("com.blankj:utilcodex:1.31.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.1")
    kapt("androidx.lifecycle:lifecycle-compiler:2.5.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.1")

    /*implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.1")
    implementation("androidx.lifecycle:lifecycle-service:2.8.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.1")
    implementation("androidx.lifecycle:lifecycle-extensions:2.8.1")*/
    //noinspection LifecycleAnnotationProcessorWithJava8
//    implementation("androidx.lifecycle:lifecycle-compiler:2.8.1")
//    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.5.1")

    //    implementation 'com.googlecode.mp4parser:isoparser:1.1.21'
    implementation(files("libs/aspectjrt-1.7.3.jar"))
    api(files("libs/isoparser-1.0.6.jar"))

    implementation("com.google.dagger:hilt-android:2.44")
    kapt("com.google.dagger:hilt-android-compiler:2.44")
    implementation ("com.github.bumptech.glide:glide:4.11.0")


//    implementation("com.github.bumptech.glide:compiler:4.11.0")

//    object Hilt {
//        const val android = "com.google.dagger:hilt-android:2.44${Versions.hiltAndroid}"
//        const val android_compiler = "com.google.dagger:hilt-android-compiler:2.44${Versions.hiltAndroid}"
//        const val viewmodel = "androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03${Versions.hilt}"
//        const val compiler = "androidx.hilt:hilt-compiler:1.0.0-alpha03${Versions.hilt}"
//    }
}