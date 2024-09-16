plugins {
    alias(libs.plugins.simplemvi.library.module)
    alias(libs.plugins.simplemvi.publishing)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.atomicfu)
            implementation(libs.kotlin.coroutines.core)
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