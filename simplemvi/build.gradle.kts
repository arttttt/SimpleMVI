plugins {
    id(libs.plugins.simplemvi.library.module.get().pluginId)
    id(libs.plugins.simplemvi.publishing.get().pluginId)
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