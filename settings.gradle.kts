pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "SimpleMVI"

includeBuild("build-logic")

include(":sample:android")
include(":sample:shared")
include(":sample:wasmJs")
include(":simplemvi")
include(":simplemvi-annotations")
include(":simplemvi-codegen")
include(":simplemvi-composition")
include(":simplemvi-viewmodel")
