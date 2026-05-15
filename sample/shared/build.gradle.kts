import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import utils.COMPILE_SDK_VERSION
import utils.MIN_SDK_VERSION

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.google.ksp)
}

kotlin {
    android {
        namespace = "com.arttttt.simplemvi.sample.shared"
        compileSdk = COMPILE_SDK_VERSION
        minSdk = MIN_SDK_VERSION
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

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

    targets.withType<KotlinJvmTarget>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
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
