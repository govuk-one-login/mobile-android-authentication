package uk.gov.authentication

import com.android.build.gradle.BaseExtension
import uk.gov.authentication.emulator.SystemImageSource
import uk.gov.authentication.emulator.SystemImageSource.GOOGLE_ATD
import uk.gov.authentication.ext.BaseExtensions.generateDeviceConfigurations
import uk.gov.authentication.ext.BaseExtensions.generateGetHardwareProfilesTask
import java.io.FileReader

plugins {
    id("kotlin-android")
}

private val _systemImageSources = listOf(
    GOOGLE_ATD
)
val managedDeviceHardwareProfiles: Provider<List<String>> by rootProject.extra(
    rootProject.provider {
        FileReader(rootProject.file("config/managedDeviceHardwareProfiles"))
            .readLines()
            .filter { !it.trim().startsWith("#") } // remove comment lines
    }
)

/**
 * Configure both app and library modules via the [BaseExtension].
 *
 * Generates applicable Android Virtual Device (AVD) configurations via
 * [generateGetHardwareProfilesTask] output. These configuration act as Gradle managed devices
 * within a given Gradle module, generating instrumentation test tasks based on the device profiles
 * made.
 */
configure<BaseExtension> {
    /**
     * Android versions to use with the gradle managed devices. Due to how the
     * `createManagedDevice${variant}AndroidTestCoverageReport` task generates within google, it
     * depends on all managed device test tasks, instead of creating a coverage report task per
     * device ID. Therefore, this should be an [IntRange] with a single entry until fixed.
     */
    val managedApiLevels: IntRange by project.extra((30..30))
    val systemImageSources: List<SystemImageSource> by project.extra(_systemImageSources)

    generateDeviceConfigurations(
        apiLevelRange = managedApiLevels,
        hardwareProfileStrings = managedDeviceHardwareProfiles.get(),
        systemImageSources = systemImageSources
    )
}
