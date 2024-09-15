plugins {
    alias(libs.plugins.simplemvi.library.module)
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