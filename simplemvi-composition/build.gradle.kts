plugins {
    alias(libs.plugins.simplemvi.library.module)
    alias(libs.plugins.simplemvi.publishing)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":simplemvi"))

            implementation(libs.kotlin.coroutines.core)
        }
    }
}

android {
    namespace = "com.arttttt.simplemvi.composition"
}

libraryPublishing {
    artifactId = "simplemvi-composition"
    description = "Composition extension for SimpleMVI"
}