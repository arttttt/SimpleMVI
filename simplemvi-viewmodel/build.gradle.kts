plugins {
    id(libs.plugins.simplemvi.library.module.get().pluginId)
    id(libs.plugins.simplemvi.publishing.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":simplemvi"))

            implementation(libs.kotlin.coroutines.core)
            implementation(libs.jetbrains.androidx.lifecycle.viewmodel.compose)
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