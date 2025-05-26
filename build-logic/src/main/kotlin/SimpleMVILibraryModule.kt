import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import utils.configureAndroid

open class SimpleMviLibraryExtension {
    var enableWasmJs: Boolean = true
}

class SimpleMVILibraryModule : Plugin<Project> {

    override fun apply(target: Project) {
        target.pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        target.pluginManager.apply("com.android.library")

        val extension = target.extensions.create<SimpleMviLibraryExtension>("simpleMVI")

        target.extensions.configure<LibraryExtension> {
            with(target) {
                configureAndroid()
            }
        }

        target.afterEvaluate {
            target.configure(extension)
        }
    }

    private fun Project.configure(
        extension: SimpleMviLibraryExtension,
    ) {
        extensions.configure<KotlinMultiplatformExtension> {
            androidTarget {
                publishLibraryVariants("debug", "release")
            }
            iosX64()
            iosArm64()
            iosSimulatorArm64()

            if (extension.enableWasmJs) {
                wasmJs {
                    browser()
                }
            }
            jvm()

            explicitApi()

            targets.configureEach {
                when (this) {
                    is KotlinAndroidTarget ->
                        compilerOptions {
                            jvmTarget.set(JvmTarget.JVM_17)
                        }

                    is KotlinJvmTarget ->
                        compilerOptions {
                            jvmTarget.set(JvmTarget.JVM_17)
                        }
                }
            }
        }
    }
}
