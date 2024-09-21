import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import utils.COMPILE_SDK_VERSION
import utils.MIN_SDK_VERSION

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

kotlin {
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(project(":simplemvi"))
            api(project(":simplemvi-logging"))

            implementation(libs.kotlin.coroutines.core)
        }
    }

    targets.configureEach {
        when (this) {
            is KotlinAndroidTarget ->
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_1_8)
                }

            is KotlinJvmTarget ->
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                }
        }
    }
}

android {
    namespace = "com.arttttt.simplemvi.sample.shared"

    compileSdk = COMPILE_SDK_VERSION

    defaultConfig {
        minSdk = MIN_SDK_VERSION
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}