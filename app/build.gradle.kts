plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    kotlin("plugin.serialization") version "2.1.10"
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")

}
fun String.runCommand(): String =
    ProcessBuilder(*split(" ").toTypedArray()).redirectErrorStream(true).start().inputStream.bufferedReader().readText().trim()

android {
    namespace = "co.ec.amazonfiyattakip"
    compileSdk = 35

    defaultConfig {
        applicationId = "co.ec.amazonfiyattakip"
        minSdk = 24
        targetSdk = 35
        versionCode = "git rev-list --count HEAD".runCommand().toInt()
        versionName = "1.0.${"git rev-list --count HEAD".runCommand()}-${"git rev-parse --abbrev-ref HEAD".runCommand()}+${"git rev-parse --short HEAD".runCommand()}${if ("git status --porcelain".runCommand().isNotEmpty()) "-dirty" else ""}"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}
composeCompiler {
}
dependencies {

    implementation(libs.kotlin.reflect)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.animation.graphics.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.cnsyn.android.helper) {
        isChanging = true
    }

    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    annotationProcessor(libs.androidx.room.compiler)
    ksp(libs.androidx.room.compiler)

    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json.v180)
    implementation(libs.ksoup.okio)


    implementation(libs.androidx.runtime)
    implementation(libs.androidx.runtime.livedata)

    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.work.runtime.ktx)


    implementation(libs.coil.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.ui.text.google.fonts)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.accompanist.permissions)



}
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
