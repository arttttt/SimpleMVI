import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.simplemvi.publishing)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":simplemvi-annotations"))
    implementation(libs.google.ksp.api)
    implementation(libs.square.kotlinpoet.lib)
    implementation(libs.square.kotlinpoet.ksp)
}

libraryPublishing {
    artifactId = "simplemvi-codegen"
    description = "KSP processors for SimpleMVI"
}
