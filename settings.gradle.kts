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
        maven("https://jitpack.io") // ✅ Jitpack 레포지토리 추가
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) // ✅ PREFER_SETTINGS로 변경
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io") // ✅ Jitpack 레포지토리 추가
    }
}

rootProject.name = "Bloom"
include(":app")
