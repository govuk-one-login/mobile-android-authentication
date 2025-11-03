import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import uk.gov.pipelines.config.ApkConfig

plugins {
    `maven-publish`
    alias(libs.plugins.kotlin.serlialization)
    id("uk.gov.pipelines.android-lib-config")
}

android {
    defaultConfig {
        defaultConfig {
            val apkConfig: ApkConfig by project.rootProject.extra
            namespace = apkConfig.applicationId + ".json"
            compileSdk = apkConfig.sdkVersions.compile
            minSdk = apkConfig.sdkVersions.minimum
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
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

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    ktlint {
        version = libs.versions.ktlint.cli.get()
    }

    lint {
        val configDir = "${rootProject.projectDir}/config"

        abortOnError = true
        absolutePaths = true
        baseline = File("$configDir/android/baseline.xml")
        checkAllWarnings = true
        checkDependencies = false
        checkGeneratedSources = false
        checkReleaseBuilds = true
        disable.addAll(
            setOf(
                "ConvertToWebp",
                "UnusedIds",
                "VectorPath",
            ),
        )
        explainIssues = true
        htmlReport = true
        ignoreTestSources = true
        ignoreWarnings = false
        lintConfig = File("$configDir/android/lint.xml")
        noLines = false
        quiet = false
        showAll = true
        textReport = true
        warningsAsErrors = true
        xmlReport = true
    }
}

dependencies {
    listOf(
        kotlin("test"),
        kotlin("test-junit"),
        libs.bundles.android.test,
        libs.bundles.mockito,
        libs.bundles.espresso,
    ).forEach(::androidTestImplementation)

    listOf(
        libs.androidx.core.core.ktx,
        libs.kotlinx.serialization.json,
        libs.jose4j,
        libs.gson,
        libs.bouncy.castle,
    ).forEach(::implementation)

    listOf(
        kotlin("test"),
        kotlin("test-junit5"),
        libs.bundles.test,
        platform(libs.junit.bom),
        libs.mockito.kotlin,
        libs.mockito.inline,
    ).forEach(::testImplementation)

    listOf(
        libs.androidx.test.orchestrator,
    ).forEach {
        androidTestUtil(it)
    }
}
