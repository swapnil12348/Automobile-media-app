pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
    versionCatalogs {
        create("libs") {
            library("ksp", "com.google.devtools.ksp:symbol-processing-api:1.9.22-1.0.17")
        }
    }
}

rootProject.name = "AutomobileMediaApp"
include(":mobile")
include(":automotive")
include(":shared")
