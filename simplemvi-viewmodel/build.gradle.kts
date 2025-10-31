plugins {
    alias(libs.plugins.simplemvi.library.module)
    alias(libs.plugins.simplemvi.publishing)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":simplemvi"))

            implementation(libs.kotlin.coroutines.core)
            implementation(libs.jetbrains.androidx.lifecycle.viewmodel.compose)
            implementation(libs.kotlin.serialization.json)
        }

        androidMain.dependencies {
            implementation(libs.androidx.lifecycle.viewmodel.savedstate)
        }
    }
}

android {
    namespace = "com.arttttt.simplemvi.viewmodel"
}

libraryPublishing {
    artifactId = "simplemvi-viewmodel"
    description = "ViewModel extensions for SimpleMVI"
}