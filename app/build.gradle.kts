// ====================================================================================
// FILE: EnglishApp/app/build.gradle.kts (Thư mục của module app)
// MỤC ĐÍCH: Cấu hình và khai báo các thư viện phụ thuộc cho module ứng dụng.
// ====================================================================================

plugins {
    // Áp dụng các plugin đã khai báo ở file gốc.
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.englishapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.englishapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Chỉ định trình chạy test cho instrumented tests
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        // Yêu cầu mã nguồn Java tương thích với Java 17.
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        // Chỉ định toolchain cho Kotlin là 17 để đảm bảo tính nhất quán.
        jvmToolchain(17)
    }

    buildFeatures {
        // Bật tính năng Jetpack Compose.
        compose = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // ----- JETPACK COMPOSE -----
    // Sử dụng Compose Bill of Materials (BOM) để quản lý phiên bản các thư viện Compose.
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.navigation:navigation-compose:2.8.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // ----- FIREBASE -----
    // Sử dụng Firebase Bill of Materials (BOM) để quản lý phiên bản.
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database") // Đã có phiên bản 20.3.0

    // ----- DATABASE (ROOM) -----
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1") // Dùng ksp thay cho annotationProcessor

    // ----- NETWORK (RETROFIT) -----
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ----- COROUTINES -----
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // ----- DEPENDENCY INJECTION (KOIN) -----
    implementation("io.insert-koin:koin-androidx-compose:3.5.3")
    implementation("io.insert-koin:koin-android:3.5.0")

    // ----- GOOGLE GENERATIVE AI (GEMINI) -----
    implementation("com.google.ai.client.generativeai:generativeai:0.1.2")

    // ====================================================================================
    // PHẦN THƯ VIỆN TEST
    // ====================================================================================

    // ----- THƯ VIỆN CHO UNIT TEST (chạy trên máy tính) -----
    testImplementation("junit:junit:4.13.2")

    // ----- THƯ VIỆN CHO INSTRUMENTED TEST (chạy trên máy ảo/thiết bị thật) -----
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // ----- THƯ VIỆN CHO TEST UI VỚI COMPOSE -----
    // ✅✅✅ SỬA LỖI TẠI ĐÂY: Khai báo lại BOM cho môi trường `androidTest`.
    // Điều này giúp Gradle biết được phiên bản của các thư viện test Compose.
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.00"))

    // Bây giờ Gradle sẽ tự động biết phiên bản cho các thư viện dưới đây thông qua BOM ở trên.
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    // ====================================================================================
}
