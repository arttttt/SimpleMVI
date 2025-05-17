plugins {
    id(libs.plugins.simplemvi.library.module.get().pluginId)
    id(libs.plugins.simplemvi.publishing.get().pluginId)
}

simpleMVI {
    enableWasmJs = false
}

kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            isStatic = true

            export(libs.androidx.lifecycle.viewmodel.lib)
            export(libs.androidx.lifecycle.viewmodel.savedstate)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":simplemvi"))

            implementation(libs.kotlin.coroutines.core)

            api(libs.androidx.lifecycle.viewmodel.lib)
            api(libs.androidx.lifecycle.viewmodel.savedstate)
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