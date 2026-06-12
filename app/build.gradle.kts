
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    //id("kotlin-parcelize")
    id ("com.google.devtools.ksp")

}

android {
    namespace = "com.QYqx.mbili"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.QYqx.mbili"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        viewBinding =true
        dataBinding =true
    }
}

dependencies {
    implementation ("androidx.appcompat:appcompat:1.0.0")               //必须 1.0.0 以上
    implementation  ("io.github.scwang90:refresh-layout-kernel:2.1.0")
    implementation  ("io.github.scwang90:refresh-header-material:2.1.0")
    implementation  ("io.github.scwang90:refresh-footer-classics:2.1.0")
    implementation ("androidx.viewpager2:viewpager2:1.0.0")
    implementation ("androidx.recyclerview:recyclerview:1.2.1")
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    implementation ("io.reactivex.rxjava3:rxjava:3.0.0")
    implementation ("io.github.youth5201314:banner:2.2.3")
    implementation ("io.reactivex.rxjava3:rxandroid:3.0.0")
    implementation ("com.google.android.material:material:1.4.0")
    implementation ("androidx.viewpager2:viewpager2:1.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1") // JSON 解析（可选，也可使用 Gson）
    implementation ("org.greenrobot:eventbus:3.3.1")
    implementation("androidx.media3:media3-common:1.4.0")
    implementation("androidx.media3:media3-exoplayer:1.4.0")
    implementation ("com.google.android.material:material:1.11.0+")
    // UI 组件
    implementation ("androidx.media3:media3-ui:1.4.0")
    // 可选 DASH 支持
    implementation ("androidx.media3:media3-exoplayer-dash:1.4.0")
    // 可选 HLS 支持
    implementation ("androidx.media3:media3-exoplayer-hls:1.4.0")
    // 可选，支持 RTSP
    implementation ("androidx.media3:media3-exoplayer-rtsp:1.4.0")

    //implementation("com.danikula:videocache:2.7.1")
    val room_version = "2.6.0"
    implementation("androidx.room:room-runtime:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    // Kotlin 扩展和协程支持
    implementation("androidx.room:room-ktx:$room_version")

    implementation ("com.tencent:mmkv-static:1.3.3") // 推荐使用 static 版本以减小包体积
    // MVVM 依赖
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    // retrofit
    // https://github.com/square/retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    // 使用 gson 解析 json
    // https://github.com/google/gson
    implementation ("com.google.code.gson:gson:2.9.0")
    // 适配 retrofit 使用 gson 解析
    // 版本要和 retrofit 一样
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.legacy.support.v4)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}