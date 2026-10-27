import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvmToolchain(17)

    androidTarget {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
    }
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
        iosX64(),
    )

    sourceSets {
        commonMain {
            kotlin.srcDir("src/main/java")
            kotlin.exclude("me/lucid/common/android/**")
            kotlin.exclude("me/lucid/common/platform/android/**")
            dependencies {
                api(project(":shared"))
                api(libs.kotlinx.serialization.json)
                api(libs.kotlinx.coroutines.core)
                api(libs.kotlinx.datetime)
            }
        }
        androidMain {
            kotlin.srcDir("src/main/java")
            kotlin.include("me/lucid/common/android/**")
            kotlin.include("me/lucid/common/platform/android/**")
            dependencies {
                api(libs.okhttp)
                api(libs.okhttp.sse)
                api(libs.okhttp.logging)
                api("io.github.petterpx:floatingx:2.3.7")
                api("io.github.petterpx:floatingx-compose:2.3.7")
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.appcompat)
                implementation(libs.material)
            }
        }
        androidUnitTest.dependencies {
            implementation(libs.junit)
        }
    }
}

android {
    namespace = "me.lucid.common"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
