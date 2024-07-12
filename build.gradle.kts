buildscript {
    dependencies {
        listOf(
            libs.jacoco.agent,
            libs.jacoco.ant,
            libs.jacoco.core,
            libs.jacoco.report,
        ).forEach {
            classpath(it)
        }
    }

    val baseNamespace by rootProject.extra { "uk.gov.android.authentication" }
    val configDir by rootProject.extra { "${rootProject.rootDir}/config" }

    val localProperties = java.util.Properties()
    if (rootProject.file("local.properties").exists()) {
        println(localProperties)
        localProperties.load(java.io.FileInputStream(rootProject.file("local.properties")))
    }

    fun findPackageVersion(): String {
        var version = "1.0.0"

        if (rootProject.hasProperty("packageVersion")) {
            version = rootProject.property("packageVersion") as String
        } else if (localProperties.getProperty("packageVersion") != null) {
            version = localProperties.getProperty("packageVersion") as String
        }

        return version
    }

    val packageVersion by rootProject.extra { findPackageVersion() }

    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }

    val getVersionCode: () -> Int = {
        val code: Int =
            if (rootProject.hasProperty("versionCode")) {
                rootProject.property("versionCode").toString().toInt()
            } else if (localProperties.getProperty("versionCode") != null) {
                localProperties.getProperty("versionCode").toString().toInt()
            } else {
                throw Error(
                    "Version code was not found as a command line parameter or a local property",
                )
            }

        println("VersionCode is set to $code")
        code
    }

    val getVersionName: () -> String = {
        val name: String =
            if (rootProject.hasProperty("versionName")) {
                rootProject.property("versionName") as String
            } else if (localProperties.getProperty("versionName") != null) {
                localProperties.getProperty("versionName") as String
            } else {
                throw Error(
                    "Version name was not found as a command line parameter or a local property",
                )
            }

        println("VersionName is set to $name")
        name
    }

    val versionCode: Int by rootProject.extra(
        getVersionCode()
    )
    val versionName: String by rootProject.extra(
        getVersionName()
    )
}

plugins {
    id("uk.gov.authentication.vale-config")
    id("uk.gov.sonar.root-config")
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
}
