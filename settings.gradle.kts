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
include(":json")
include(":localauth")

includeBuild("${rootProject.projectDir}/mobile-android-pipelines/buildLogic")
gradle.startParameter.excludedTaskNames.addAll(listOf(":buildLogic:plugins:testClasses"))

fun setGithubCredentials(): MavenArtifactRepository.() -> Unit = {
    val (credUser, credToken) = fetchGithubCredentials()
    credentials {
        username = credUser
        password = credToken
    }
}

fun fetchGithubCredentials(): Pair<String, String> {
    val gprUser = System.getenv("GITHUB_ACTOR")
    val gprToken = System.getenv("GITHUB_TOKEN")

    if (!gprUser.isNullOrEmpty() && !gprToken.isNullOrEmpty()) {
        return Pair(gprUser, gprToken)
    }

    val gprUserProperty = providers.gradleProperty("gpr.user")
    val gprTokenProperty = providers.gradleProperty("gpr.token")

    return gprUserProperty.get() to gprTokenProperty.get()
}
