import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
}

val keystorePropertiesFile: File = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

val versionPropertiesFile: File = rootProject.file("version.properties")
val versionProperties = Properties().apply {
    load(FileInputStream(versionPropertiesFile))
}

fun getApiKey(propertyKey: String): String {
    return gradleLocalProperties(rootDir, providers).getProperty(propertyKey)
}

android {
    namespace = "com.sangyoon.vehiclenote"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.sangyoon.vehiclenote"
        minSdk = 24
        targetSdk = 36
        versionCode = versionProperties.getProperty("VERSION_CODE").toInt()
        versionName = versionProperties.getProperty("VERSION_NAME")
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDefault = true
            isDebuggable = true
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // 모듈 의존성 — app은 조립(composition root)만 담당
    implementation(project(":core:domain"))
    implementation(project(":core:data"))   // DI 모듈(Database/Repository) 조립용
    implementation(project(":core:ocr"))    // OcrModule 바인딩용
    implementation(project(":core:ui"))     // AppTheme
    implementation(project(":feature:vehicle"))
    implementation(project(":feature:entryexit"))
    implementation(project(":feature:settings"))

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.compose.foundation.layout)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Room (DatabaseModule 조립)
    implementation(libs.androidx.room.ktx)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Icons (VnBottomTabs)
    implementation(libs.androidx.compose.material.icons.extended)

    // DataStore (DataStoreModule 조립)
    implementation(libs.androidx.datastore.preferences)

    // Firebase (google-services 플러그인은 app에만 적용 가능)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
