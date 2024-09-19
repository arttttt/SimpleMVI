import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import utils.configureAndroid
import utils.libs
import utils.pluginId

class SimpleMVILibraryModule : Plugin<Project> {

    override fun apply(target: Project) {
        target.configure()
    }

    private fun Project.configure() {
        pluginManager.apply(libs.pluginId("kotlin-multiplatform"))
        pluginManager.apply(libs.pluginId("android-library"))

        extensions.configure<KotlinMultiplatformExtension> {
            androidTarget {
                publishLibraryVariants("debug", "release")
            }
            iosX64()
            iosArm64()
            iosSimulatorArm64()

            explicitApi()

            targets.configureEach {
                when (this) {
                    is KotlinAndroidTarget ->
                        compilerOptions {
                            jvmTarget.set(JvmTarget.JVM_1_8)
                        }

                    is KotlinJvmTarget ->
                        compilerOptions {
                            jvmTarget.set(JvmTarget.JVM_11)
                        }
                }
            }
        }

        extensions.configure<LibraryExtension> {
            configureAndroid()
        }
    }
}