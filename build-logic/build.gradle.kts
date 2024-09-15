import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.android.gradle.plugin)
    implementation(libs.jetbrains.kotlin.gradle.plugin)
}

gradlePlugin {

    plugins.register("simplemvi-library-module") {

        id = "simplemvi-library-module"
        implementationClass = "SimpleMVILibraryModule"
        version = "1.0.0"
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
    }
}