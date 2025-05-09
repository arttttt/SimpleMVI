plugins {
    id(libs.plugins.simplemvi.library.module.get().pluginId)
    id(libs.plugins.simplemvi.publishing.get().pluginId)
}

simpleMVI {
    enableWasmJs = false
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":simplemvi"))

            implementation(libs.kotlin.coroutines.core)
            implementation(libs.androidx.lifecycle.viewmodel.lib)
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