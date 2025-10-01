plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.google.ksp)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(project(":simplemvi-annotations"))
    implementation(libs.google.ksp.api)
    implementation(libs.square.kotlinpoet.lib)
    implementation(libs.square.kotlinpoet.ksp)
}