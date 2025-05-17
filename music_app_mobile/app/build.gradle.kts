plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.group06.music_app_mobile"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.group06.music_app_mobile"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
    }

}
//9B:26:D2:30:60:DB:F0:39:0E:98:2A:7B:69:0D:31:11:EC:2C:E1:10
dependencies {
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)
    implementation(libs.retrofit2)
    implementation(libs.gson)
    compileOnly("org.projectlombok:lombok:1.18.30") // (chỉ biên dịch)
    annotationProcessor("org.projectlombok:lombok:1.18.30") // (xử lý annotation)
    implementation(libs.viewpager2)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}