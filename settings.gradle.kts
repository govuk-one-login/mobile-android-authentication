pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()

        maven(
            "https://maven.pkg.github.com/govuk-one-login/mobile-android-ui",
            setGithubCredentials()
        )
    }
}

rootProject.name = "mobile-android-authentication"
include(":app")

includeBuild("${rootProject.projectDir}/mobile-android-pipelines/buildLogic")
gradle.startParameter.excludedTaskNames.addAll(listOf(":buildLogic:plugins:testClasses"))
include(":localauth")

fun setGithubCredentials(): MavenArtifactRepository.() -> Unit = {
    credentials {
        username = providers.gradleProperty("gpr.user").get()
        password = providers.gradleProperty("gpr.token").get()
    }
}
include(":test")
