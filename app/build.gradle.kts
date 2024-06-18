import uk.gov.authentication.config.ApkConfig

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kotlin.serlialization)
    id("maven-publish")
    id("uk.gov.authentication.jvm-toolchains")
    id("uk.gov.authentication.vale-config")
    id("uk.gov.authentication.sonarqube-module-config")
    id("jacoco")
    id("uk.gov.authentication.jacoco-module-config")
    id("uk.gov.authentication.emulator-config")
}

android {
    defaultConfig {
        namespace = ApkConfig.APPLICATION_ID + ".impl"
        compileSdk = ApkConfig.COMPILE_SDK_VERSION
        minSdk = ApkConfig.MINIMUM_SDK_VERSION
        targetSdk = ApkConfig.TARGET_SDK_VERSION
        testInstrumentationRunner = namespace + ".InstrumentationTestRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
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

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        animationsDisabled = true
        unitTests.all {
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    listOf(
        libs.androidx.test.ext.junit,
        libs.espresso.core,
    ).forEach(::androidTestImplementation)

    listOf(
        libs.androidx.core.core.ktx,
        libs.appcompat,
        libs.appauth,
        libs.kotlinx.serialization.json,
    ).forEach(::implementation)

    listOf(
        libs.junit,
    ).forEach(::testImplementation)
}

publishing {
    publications {
        create<MavenPublication>("mobile-android-authentication") {
            groupId = "uk.gov.android"
            artifactId = "authentication"
            version = rootProject.extra["packageVersion"] as String

            artifact("$buildDir/outputs/aar/${project.name}-release.aar")

            // generate pom nodes for dependencies
            pom.withXml {
                val dependenciesNode = asNode().appendNode("dependencies")
                configurations.getByName("implementation") {
                    allDependencies.forEach { dependency ->
                        if (dependency.name != "unspecified") {
                            val dependencyNode = dependenciesNode.appendNode("dependency")
                            dependencyNode.appendNode("groupId", dependency.group)
                            dependencyNode.appendNode("artifactId", dependency.name)
                            dependencyNode.appendNode("version", dependency.version)
                        }
                    }
                }
            }
        }
        repositories {
            maven("https://maven.pkg.github.com/govuk-one-login/mobile-android-authentication") {
                credentials {
                    username = System.getenv("USERNAME")
                    password = System.getenv("TOKEN")
                }
            }
        }
    }
}
