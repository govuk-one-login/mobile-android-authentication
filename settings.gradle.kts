pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "mobile-android-authentication"
include(":app")

includeBuild("${rootProject.projectDir}/mobile-android-pipelines/buildLogic")
gradle.startParameter.excludedTaskNames.addAll(listOf(":buildLogic:plugins:testClasses"))
