plugins {
    alias(libs.plugins.simplemvi.library.module)
    alias(libs.plugins.simplemvi.publishing)
}

android {
    namespace = "com.arttttt.simplemvi.annotations"
}

libraryPublishing {
    artifactId = "simplemvi-annotations"
    description = "KSP annotations for SimpleMVI"
}