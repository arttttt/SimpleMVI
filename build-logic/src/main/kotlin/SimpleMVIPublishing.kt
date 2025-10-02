import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.Platform
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import utils.LIBRARY_VERSION
import utils.libs
import utils.pluginId

class SimpleMVIPublishing : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("maven-publish")

        extensions.create<PublishingExtension>("libraryPublishing")

        pluginManager.withPlugin(libs.pluginId("kotlin-multiplatform")) {
            configureVanniktech(
                publication = KotlinMultiplatform(
                    sourcesJar = true,
                    androidVariantsToPublish = listOf("debug", "release"),
                )
            )
        }

        pluginManager.withPlugin(libs.pluginId("kotlin-jvm")) {
            configureVanniktech(
                publication = KotlinJvm(
                    sourcesJar = true
                )
            )
        }
    }

    private fun Project.configureVanniktech(publication: Platform) {
        pluginManager.apply(libs.pluginId("vanniktech-maven-publish"))

        configureCommonPomAndCoordinates()

        extensions.configure<MavenPublishBaseExtension> {
            publishToMavenCentral()
            signAllPublications()
            configure(publication)
        }
    }

    private fun Project.configureCommonPomAndCoordinates() {
        val publishingExtension = extensions.getByType<PublishingExtension>()

        extensions.configure<MavenPublishBaseExtension> {
            coordinates(
                "io.github.arttttt.simplemvi",
                publishingExtension.artifactId,
                LIBRARY_VERSION
            )

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

