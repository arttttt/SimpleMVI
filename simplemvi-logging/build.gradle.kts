plugins {
    alias(libs.plugins.simplemvi.library.module)
    alias(libs.plugins.simplemvi.publishing)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":simplemvi"))
        }
    }
}

android {
    namespace = "com.arttttt.simplemvi.logging"
}

libraryPublishing {
    artifactId = "simplemvi-logging"
    description = "Logging functionality for SimpleMVI"
}