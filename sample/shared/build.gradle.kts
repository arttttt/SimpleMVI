import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinCommonCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMetadataTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import utils.COMPILE_SDK_VERSION
import utils.MIN_SDK_VERSION

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.google.ksp)
    id("storeGraphPlugin")
}

kotlin {
    androidTarget()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true

            export(project(":simplemvi"))

            export(libs.kotlin.coroutines.core)
        }
    }

    wasmJs {
        browser()
    }

    sourceSets {
        commonMain {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")

            dependencies {
                api(project(":simplemvi"))
                implementation(project(":simplemvi-annotations"))

                api(libs.kotlin.coroutines.core)
            }
        }
    }

    targets.configureEach {
        when (this) {
            is KotlinAndroidTarget ->
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }

            is KotlinJvmTarget ->
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    kspCommonMainMetadata(project(":simplemvi-codegen"))
}

kotlin.targets.configureEach {
    compilations.configureEach {
        compileTaskProvider.configure {
            dependsOn(tasks.named("kspCommonMainKotlinMetadata"))
        }
    }
}