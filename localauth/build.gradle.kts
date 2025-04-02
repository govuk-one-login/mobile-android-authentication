import uk.gov.pipelines.config.ApkConfig

plugins {
    `maven-publish`
    alias(libs.plugins.compose.compiler)
    id("uk.gov.pipelines.android-lib-config")
}

android {
    namespace = "uk.gov.android.localauth"

    defaultConfig {
        val apkConfig: ApkConfig by project.rootProject.extra
        namespace = apkConfig.applicationId + ".localauth"
        compileSdk = apkConfig.sdkVersions.compile
        minSdk = apkConfig.sdkVersions.minimum
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        animationsDisabled = true
        unitTests.all {
            it.useJUnitPlatform()
            it.testLogging {
                events =
                    setOf(
                        org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
                        org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
                        org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED,
                    )
            }
        }
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    ktlint {
        version = libs.versions.ktlint.cli.get()
    }
}

dependencies {
    listOf(
        kotlin("test"),
        kotlin("test-junit"),
        libs.android.test.ext.junit,
        libs.espresso.core,
    ).forEach(::androidTestImplementation)

    listOf(
        libs.androidx.core.core.ktx,
        libs.appcompat,
        libs.material,
        libs.androidx.biometric,
        libs.androidx.ui,
        libs.androidx.material3,
        libs.androidx.ui.tooling.preview,
        libs.androidx.navigation,
        libs.bundles.gov.uk,
        libs.kotlinx.collections.immutable,
    ).forEach(::implementation)

    listOf(
        kotlin("test"),
        kotlin("test-junit5"),
        libs.bundles.test,
        platform(libs.junit.bom),
        libs.mockito.kotlin,
    ).forEach(::testImplementation)

    listOf(
        libs.androidx.test.orchestrator,
    ).forEach {
        androidTestUtil(it)
    }

    testRuntimeOnly(libs.junit.vintage.engine)
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
