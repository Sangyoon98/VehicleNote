pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "VehicleNote"
include(":app")
include(":core:domain")
include(":core:data")
include(":core:ocr")
include(":core:ui")
include(":core:analytics")
include(":feature:vehicle")
include(":feature:entryexit")
include(":feature:settings")
