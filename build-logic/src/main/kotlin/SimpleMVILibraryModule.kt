import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import utils.COMPILE_SDK_VERSION
import utils.MIN_SDK_VERSION
import utils.libs
import utils.pluginId

class SimpleMVILibraryModule : Plugin<Project> {

    override fun apply(target: Project) {
        target.configure()
    }

    private fun Project.configure() {
        pluginManager.apply(libs.pluginId("kotlin-multiplatform"))
        pluginManager.apply(libs.pluginId("android-kotlin-multiplatform-library"))

        extensions.configure<KotlinMultiplatformExtension> {
            iosX64()
            iosArm64()
            iosSimulatorArm64()
            wasmJs {
                browser()
            }
            jvm()

            explicitApi()

            targets.withType<KotlinMultiplatformAndroidLibraryTarget>().configureEach {
                compileSdk = COMPILE_SDK_VERSION
                minSdk = MIN_SDK_VERSION
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
            }

            targets.withType<KotlinJvmTarget>().configureEach {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
            }
        }
    }
}
