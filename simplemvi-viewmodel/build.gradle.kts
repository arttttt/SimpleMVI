plugins {
    alias(libs.plugins.simplemvi.library.module)
    alias(libs.plugins.simplemvi.publishing)
}

kotlin {
    android {
        namespace = "com.arttttt.simplemvi.viewmodel"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":simplemvi"))

            implementation(libs.kotlin.coroutines.core)
            implementation(libs.jetbrains.androidx.lifecycle.viewmodel.compose)
        }
    }
}

libraryPublishing {
    artifactId = "simplemvi-viewmodel"
    description = "ViewModel extensions for SimpleMVI"
}