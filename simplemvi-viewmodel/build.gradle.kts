plugins {
    alias(libs.plugins.simplemvi.library.module)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":simplemvi"))
        }

        androidMain.dependencies {
            implementation(libs.kotlin.coroutines.core)
            implementation(libs.androidx.lifecycle.viewmodel.ktx)
        }
    }
}

android {
    namespace = "com.arttttt.simplemvi.viewmodel"
}