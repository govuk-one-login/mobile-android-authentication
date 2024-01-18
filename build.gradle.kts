buildscript {
    val jacocoVersion by rootProject.extra("0.8.9")
    val minAndroidVersion by rootProject.extra { 29 }
    val compileAndroidVersion by rootProject.extra { 34 }
    val androidBuildToolsVersion by rootProject.extra { "34.0.0" }
    val configDir by rootProject.extra { "$rootDir/config" }
    val baseNamespace by rootProject.extra { "uk.gov.android.authentication" }

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
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.kotlin) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.kotlin.serlialization) apply false
    id("org.sonarqube") version "4.4.1.3373"
    id("sonarqube-root-config")
}

apply {
    from("$rootDir/config/styles/tasks.gradle")
}
