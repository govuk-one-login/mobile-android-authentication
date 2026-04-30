import uk.gov.android.authentication.extensions.setNamespace

plugins {
    `maven-publish`
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.paparazzi)
    id("uk.gov.android.authentication.android-lib-config")
}

android {
    setNamespace(".localauth")

    buildFeatures {
        compose = true
    }

    ktlint {
        version = libs.versions.ktlint.cli.get()
    }
}

dependencies {
    listOf(
        libs.androidx.biometric,
        libs.androidx.core.core.ktx,
        libs.androidx.material3,
        libs.androidx.navigation,
        libs.androidx.ui,
        libs.androidx.ui.tooling.preview,
        libs.appcompat,
        libs.bundles.gov.uk,
        libs.kotlinx.collections.immutable,
        libs.material,
    ).forEach(::implementation)

    listOf(
        libs.androidx.compose.ui.tooling,
        libs.androidx.compose.ui.test.manifest,
    ).forEach(::debugImplementation)
}

mavenPublishingConfig {
    mavenConfigBlock {
        name.set(
            "Local Authentication (secure device) module for Android Devices",
        )
        description.set(
            """
            A Gradle module which implements the local authentication (passcode/ biometrics) based on
            a combination of device security and privacy settings and user choice of how to secure the application.
            """.trimIndent(),
        )
    }
}

// https://govukverify.atlassian.net/browse/DCMAW-11888
// https://github.com/Kotlin/dokka/issues/2956
tasks.matching { task ->
    task.name.contains("javaDocReleaseGeneration") ||
        task.name.contains("javaDocDebugGeneration")
}.configureEach {
    enabled = false
}
