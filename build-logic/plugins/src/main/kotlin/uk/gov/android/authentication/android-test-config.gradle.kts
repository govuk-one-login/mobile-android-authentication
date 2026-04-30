package uk.gov.android.authentication

import com.android.build.api.dsl.LibraryExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.kotlin

//https://github.com/gradle/gradle/issues/15383
val libs = the<LibrariesForLibs>()

plugins.apply("uk.gov.android.authentication.kotlin-test-config")

configure<LibraryExtension> {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        animationsDisabled = true
        unitTests.all {
            it.useJUnitPlatform()
            it.testLogging {
                events = setOf(
                    TestLogEvent.FAILED,
                    TestLogEvent.PASSED,
                    TestLogEvent.SKIPPED,
                )
            }
        }
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    "androidTestImplementation"(kotlin("test"))
    "androidTestImplementation"(kotlin("test-junit"))
    "androidTestImplementation"(libs.bundles.android.test)
    "androidTestImplementation"(libs.bundles.espresso)
    "androidTestImplementation"(libs.bundles.mockito)

    "androidTestUtil"(libs.androidx.test.orchestrator)

    "testImplementation"(libs.androidx.compose.ui.junit4)
    "testImplementation"(libs.espresso.core)
    "testImplementation"(libs.espresso.intents)
    "testImplementation"(libs.robolectric)
}
