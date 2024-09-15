plugins {
    alias((libs.plugins.simplemvi.library.module))
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.atomicfu)
            implementation(libs.kotlin.coroutines.core)
        }

        androidMain.dependencies {
            implementation(libs.androidx.lifecycle.viewmodel.ktx)
        }
    }
}

android {
    namespace = "com.arttttt.simplemvi"
}
