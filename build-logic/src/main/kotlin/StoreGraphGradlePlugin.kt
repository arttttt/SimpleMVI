import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class StoreGraphGradlePlugin : KotlinCompilerPluginSupportPlugin {

    override fun apply(target: Project) {
        target.extensions.create("storeGraph", StoreGraphExtension::class.java)
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> {
        val extension = kotlinCompilation.target.project.extensions
            .getByType(StoreGraphExtension::class.java)

        return kotlinCompilation.target.project.provider {
            listOf(
                SubpluginOption(
                    key = "storeGraphOutputDir",
                    value = extension.outputDir.get()
                )
            )
        }
    }

    override fun getCompilerPluginId(): String =
        "com.arttttt.simplemvi.compiler.storegraph"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "io.github.arttttt.simplemvi",
        artifactId = "simplemvi-codegen",
        version = "1.0"
    )
}

abstract class StoreGraphExtension {
    abstract val outputDir: Property<String>

    init {
        outputDir.convention("build/generated/store-graphs")
    }
}