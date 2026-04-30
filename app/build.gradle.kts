import uk.gov.android.authentication.extensions.setNamespace

plugins {
    `maven-publish`
    alias(libs.plugins.kotlin.serlialization)
    id("uk.gov.android.authentication.android-lib-config")
}

apply(from = "${rootProject.extra["configDir"]}/ktlint/config.gradle")

android {
    setNamespace(".impl")

    packaging {
        resources.excludes.add("META-INF/versions/9/OSGI-INF/MANIFEST.MF")
    }
}

dependencies {
    listOf(
        libs.androidx.uiautomator,
        libs.logging.test
    ).forEach(::androidTestImplementation)

    listOf(
        libs.androidx.biometric,
        libs.androidx.browser,
        libs.androidx.core.core.ktx, //
        libs.appauth,
        libs.appcompat, //
        libs.bouncy.castle,
        libs.gson,
        libs.jose4j,
        libs.kotlinx.coroutines,
        libs.kotlinx.serialization.json,
        libs.logging,
        platform(libs.kotlin.bom)
    ).forEach(::implementation)

    listOf(
        libs.kotlinx.coroutines.test,
        libs.logging.test,
        libs.mockito.inline
    ).forEach(::testImplementation)
}

mavenPublishingConfig {
    mavenConfigBlock {
        name.set(
            "Authentication module for Android Devices"
        )
        description.set(
            """
            A Gradle module which implements OpenID Connect to return an access token for Android.
            """.trimIndent()
        )
    }
}
