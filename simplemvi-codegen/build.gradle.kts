import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.simplemvi.publishing)
    `java-gradle-plugin`
    `kotlin-dsl`
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

    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin-api")
    implementation(gradleApi())
}

libraryPublishing {
    artifactId = "simplemvi-codegen"
    description = "KSP processors for SimpleMVI"
}

gradlePlugin {
    plugins {
        register("storeGraphPlugin") {
            id = "storeGraphPlugin"
            implementationClass = "StoreGraphGradlePlugin"
        }
    }
}