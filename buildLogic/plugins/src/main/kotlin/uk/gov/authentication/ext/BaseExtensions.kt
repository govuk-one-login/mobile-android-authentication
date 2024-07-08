package uk.gov.authentication.ext

import com.android.build.api.dsl.ManagedVirtualDevice
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.tasks.ManagedDeviceInstrumentationTestTask
import com.android.build.gradle.internal.tasks.ManagedDeviceSetupTask
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.maybeCreate
import org.gradle.kotlin.dsl.register
import uk.gov.authentication.config.ApkConfig
import uk.gov.authentication.emulator.SystemImageSource
import uk.gov.authentication.ext.ProjectExtensions.versionCode
import uk.gov.authentication.ext.ProjectExtensions.versionName
import uk.gov.authentication.ext.StringExtensions.proseToUpperCamelCase
import java.io.File

object BaseExtensions {
    private val filter = Regex("[/\\\\:<>\"?*| ()]")

    /**
     * Registers a task that defers to the `getAllHardwareProfileNames` script found within the
     * `scripts/` folder.
     *
     * Outputs all applicable hardware profiles available on the machine running this task.
     */
    fun BaseExtension.generateGetHardwareProfilesTask(
        project: Project,
        hardwareProfilesOutput: File,
    ) = project.tasks.register("getHardwareProfiles", Exec::class) {
        commandLine(
            "bash",
            "${project.rootProject.rootDir}/scripts/getAllHardwareProfileNames",
            hardwareProfilesOutput.absolutePath,
        )
        onlyIf("The output file doesn't exist") {
            !hardwareProfilesOutput.exists()
        }
    }

    /**
     * Creates a Gradle managed device within the associated Gradle project.
     *
     * This effectively creates a [ManagedDeviceSetupTask] to create, build and verify the initial
     * state of a new emulator. There is also a [ManagedDeviceInstrumentationTestTask] created,
     * respecting `${flavor}${buildType}AndroidTest` naming conventions.
     */
    private fun BaseExtension.generateManagedDeviceConfiguration(
        hardwareProfile: String,
        apiLevel: Int,
        source: SystemImageSource,
    ) {
        val managedDeviceName = generateDeviceName(hardwareProfile, source, apiLevel)

        defaultConfig {
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        testOptions {
            animationsDisabled = true
            execution = "ANDROIDX_TEST_ORCHESTRATOR"
            managedDevices {
                devices {
                    maybeCreate<ManagedVirtualDevice>(
                        managedDeviceName,
                    ).apply {
                        // Use device profiles you typically see in Android Studio.
                        this.device = hardwareProfile
                        // Use only API levels 27 and higher.
                        this.apiLevel = apiLevel
                        // To include Google services, use "google"
                        this.systemImageSource = source.image
                    }
                }
            }
        }
    }

    private fun generateDeviceName(
        hardwareProfile: String,
        source: SystemImageSource,
        apiLevel: Int,
    ): String {
        val hardwareProfileTaskSegment =
            hardwareProfile.replace(
                filter,
                "",
            ).proseToUpperCamelCase()

        val systemImageSourceTaskSegment = source.sanitise()

        return systemImageSourceTaskSegment +
            "${hardwareProfileTaskSegment.capitalized()}Api$apiLevel"
    }

    /**
     * Loops through the provided parameters, deferring each entry to the
     * [generateManagedDeviceConfiguration] function.
     */
    fun BaseExtension.generateDeviceConfigurations(
        hardwareProfileStrings: Collection<String>,
        apiLevelRange: IntRange,
        systemImageSources: Collection<SystemImageSource> = SystemImageSource.values().asList(),
    ) {
        hardwareProfileStrings.forEach { hardwareProfileString ->
            apiLevelRange.forEach { apiLevel ->
                systemImageSources.forEach { systemImageSource ->
                    generateManagedDeviceConfiguration(
                        hardwareProfile = hardwareProfileString,
                        apiLevel = apiLevel,
                        source = systemImageSource,
                    )
                }
            }
        }
    }

    fun BaseExtension.baseAndroidConfig(target: Project) {
        configureDefaultConfig(target)
    }

    @Suppress("UnstableApiUsage")
    private fun BaseExtension.configureDefaultConfig(project: Project) {
        compileSdkVersion(ApkConfig.COMPILE_SDK_VERSION)
        defaultConfig {
            minSdk = ApkConfig.MINIMUM_SDK_VERSION
            targetSdk = ApkConfig.TARGET_SDK_VERSION
            versionCode = project.versionCode
            versionName = project.versionName

            consumerProguardFiles(
                "consumer-rules.pro",
            )

            packagingOptions {
                resources.excludes += "META-INF/LICENSE-LGPL-2.1.txt"
                resources.excludes += "META-INF/LICENSE-LGPL-3.txt"
                resources.excludes += "META-INF/LICENSE-W3C-TEST"
                resources.excludes += "META-INF/DEPENDENCIES"
                resources.excludes += "*.proto"
            }

            testOptions {
                unitTests {
                    isIncludeAndroidResources = true
                }
            }
        }
    }
}
