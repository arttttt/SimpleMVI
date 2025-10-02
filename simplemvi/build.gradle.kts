plugins {
    alias(libs.plugins.simplemvi.library.module)
    alias(libs.plugins.simplemvi.publishing)
    alias(libs.plugins.kotlin.atomicfu)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.coroutines.core)
            implementation(libs.kotlin.atomicfu)
        }
    }
}

android {
    namespace = "com.arttttt.simplemvi"
}

libraryPublishing {
    artifactId = "simplemvi"
    description = "Simple, but powerful multiplatform MVI library"
}