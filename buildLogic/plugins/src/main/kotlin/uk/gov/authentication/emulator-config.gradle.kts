package uk.gov.authentication

import com.android.build.gradle.BaseExtension
import uk.gov.authentication.emulator.SystemImageSource
import uk.gov.authentication.emulator.SystemImageSource.GOOGLE_ATD
import uk.gov.authentication.emulator.SystemImageSource.GOOGLE_PLAYSTORE
import uk.gov.authentication.ext.BaseExtensions.generateDeviceConfigurations
import uk.gov.authentication.ext.BaseExtensions.generateGetHardwareProfilesTask
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.FileReader

plugins {
    id("kotlin-android")
}

private val hardwareProfileFilter: (String) -> Boolean = {
    it.contains("pixel xl", ignoreCase = true)
}
private val systemImageSources =
    listOf(
        GOOGLE_ATD,
        GOOGLE_PLAYSTORE,
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
    /* Extra properties for the plugin. Defers to the root project values. Uses the underscored variants
     * as the initial value if the root project has undefined values.
     */
    val minAndroidVersion: Int by project.extra(29)
    val targetAndroidVersion: Int by project.extra(34)
    val managedApiLevels: IntRange by project.extra((minAndroidVersion..targetAndroidVersion))
    val hardwareProfileFilter: (String) -> Boolean by project.extra(hardwareProfileFilter)
    val systemImageSources: List<SystemImageSource> by project.extra(systemImageSources)

    val consoleOutputStream = ByteArrayOutputStream()
    val hardwareProfilesList =
        rootProject.file(
            "${rootProject.buildDir}/outputs/managedDeviceHardwareProfiles.txt",
        )
    val hardwareProfilesTask = generateGetHardwareProfilesTask(project, hardwareProfilesList)

    if (!hardwareProfilesList.exists()) {
        /**
         * Call the hardware profiles task within the gradle configuration stage for the sake of
         * building out the various hardware profiles.
         */
        exec {
            commandLine = hardwareProfilesTask.get().commandLine
            args = hardwareProfilesTask.get().args
            standardOutput = consoleOutputStream
        }
    }

    val hardwareProfileStrings: List<String> =
        BufferedReader(FileReader(hardwareProfilesList))
            .readLines()

    generateDeviceConfigurations(
        apiLevelRange = managedApiLevels,
        hardwareProfileStrings = hardwareProfileStrings.filter(hardwareProfileFilter),
        systemImageSources = systemImageSources,
    )
}
