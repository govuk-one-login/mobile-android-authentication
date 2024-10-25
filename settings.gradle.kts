import org.gradle.api.internal.provider.MissingValueException

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
        maven(
            "https://maven.pkg.github.com/govuk-one-login/mobile-android-networking",
            setupGithubCredentials()
        )
    }
}

fun setupGithubCredentials(): MavenArtifactRepository.() -> Unit =
    {
        val (credUser, credToken) = fetchGithubCredentials()
        credentials {
            username = credUser
            password = credToken
        }
    }

fun fetchGithubCredentials(): Pair<String, String> {
    val gprUser = providers.gradleProperty("gpr.user")
    val gprToken = providers.gradleProperty("gpr.token")

    return try {
        gprUser.get() to gprToken.get()
    } catch (exception: MissingValueException) {
        logger.warn(
            "Could not find 'Github Package Registry' properties. Refer to the proceeding " +
                    "location for instructions:\n\n" +
                    "${rootDir.path}/docs/developerSetup/github-authentication.md\n",
            exception
        )
        println(System.getenv("USERNAME"))
        System.getenv("USERNAME") to System.getenv("TOKEN")
    }
}

rootProject.name = "mobile-android-authentication"
include(":app")

includeBuild("${rootProject.projectDir}/mobile-android-pipelines/buildLogic")
gradle.startParameter.excludedTaskNames.addAll(listOf(":buildLogic:plugins:testClasses"))
