import uk.gov.pipelines.config.ApkConfig

plugins {
    `maven-publish`
    id("uk.gov.pipelines.android-lib-config")
}

apply(from = "${rootProject.extra["configDir"]}/detekt/config.gradle")
apply(from = "${rootProject.extra["configDir"]}/ktlint/config.gradle")

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

    lint {
        val configDir = "${rootProject.projectDir}/config"

        baseline = File("$configDir/android/baseline.xml")
        lintConfig = File("$configDir/android/lint.xml")
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
                        org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
                    )
            }
        }
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    packaging {
        resources.excludes.add("META-INF/versions/9/OSGI-INF/MANIFEST.MF")
    }
}

dependencies {
    listOf(
        libs.bundles.test,
        platform(libs.junit.bom),
        libs.mockito.kotlin
    ).forEach(::testImplementation)

    listOf(
        libs.android.test.ext.junit,
        libs.espresso.core
    ).forEach(::androidTestImplementation)

    listOf(
        libs.androidx.core.core.ktx,
        libs.appcompat,
        libs.material
    ).forEach(::implementation)

    listOf(
        libs.androidx.test.orchestrator
    ).forEach {
        androidTestUtil(it)
    }
}

mavenPublishingConfig {
    mavenConfigBlock {
        name.set(
            "Local Authentication (secure device) module for Android Devices"
        )
        description.set(
            """
            A Gradle module which implements the local authentication (passcode/ biometrics) based on
            a combination of device security and privacy settings and user choice of how to secure the application.
            """.trimIndent()
        )
    }
}
