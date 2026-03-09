import java.net.URI

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
        maven { url = URI.create("https://jitpack.io") }
        maven { url = URI.create("https://raw.githubusercontent.com/iProov/android/master/maven/") }
        maven { url = URI.create("https://raw.githubusercontent.com/iProov/android/beta/maven/") }
    }
}

rootProject.name = "example-app"
include(":app")