import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import utils.LIBRARY_VERSION
import utils.libs
import utils.pluginId

class SimpleMVIPublishing : Plugin<Project> {

    override fun apply(target: Project) {
        target.configure()
    }

    private fun Project.configure() {
        pluginManager.apply("maven-publish")

        extensions.add("libraryPublishing", PublishingExtension::class.java)

        afterEvaluate {
            pluginManager.apply(libs.pluginId("vanniktech-maven-publish"))

            val publishingExtension = extensions.getByType<PublishingExtension>()

            extensions.configure<MavenPublishBaseExtension> {
                publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
                signAllPublications()

                configure(
                    KotlinMultiplatform(
                        sourcesJar = true,
                        androidVariantsToPublish = listOf("debug", "release"),
                    )
                )

                coordinates("io.github.arttttt.simplemvi", publishingExtension.artifactId, LIBRARY_VERSION)

                pom {
                    name.set("SimpleMVI")
                    description.set(publishingExtension.description)
                    inceptionYear.set("2024")
                    url.set("https://github.com/arttttt/SimpleMVI")

                    developers {
                        developer {
                            id.set("arttttt")
                            name.set("Artem Bambalov")
                            url.set("https://github.com/arttttt")
                        }
                    }

                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://github.com/arttttt/SimpleMVI/blob/master/LICENSE")
                            distribution.set("https://github.com/arttttt/SimpleMVI/blob/master/LICENSE")
                        }
                    }

                    scm {
                        url.set("https://github.com/arttttt/SimpleMVI")
                        connection.set("scm:git:git://github.com/arttttt/SimpleMVI.git")
                        developerConnection.set("scm:git:ssh://git@github.com/arttttt/SimpleMVI.git")
                    }
                }
            }
        }
    }
}