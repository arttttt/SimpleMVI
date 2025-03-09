package utils

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.VersionConstraint
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.gradle.plugin.use.PluginDependency
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.library(name: String): Provider<MinimalExternalModuleDependency> {
    return findLibrary(name).get()
}

internal fun VersionCatalog.version(name: String): VersionConstraint {
    return findVersion(name).get()
}

internal fun VersionCatalog.plugin(name: String): PluginDependency {
    return findPlugin(name).get().get()
}

internal fun VersionCatalog.pluginId(name: String): String {
    return findPlugin(name).get().get().pluginId
}

internal fun VersionCatalog.bundle(name: String): Provider<ExternalModuleDependencyBundle> {
    return findBundle(name).get()
}

internal fun DependencyHandlerScope.implementation(lib: Any) {
    add("implementation", lib)
}

internal fun DependencyHandlerScope.debugImplementation(lib: Any) {
    add("debugImplementation", lib)
}

internal fun DependencyHandlerScope.androidTestImplementation(lib: Any) {
    add("androidTestImplementation", lib)
}

internal fun DependencyHandlerScope.ksp(plugin: Any) {
    add("ksp", plugin)
}

internal fun Project.kotlinOptions(block: KotlinJvmCompilerOptions.() -> Unit) {
    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions(block)
    }
}

context(Project)
internal fun CommonExtension<*, *, *, *, *, *>.configureAndroid() {
    compileSdk = COMPILE_SDK_VERSION

    defaultConfig {
        minSdk = MIN_SDK_VERSION
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
