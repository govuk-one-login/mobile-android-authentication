package uk.gov.authentication

import com.android.build.api.dsl.LibraryExtension
import com.android.build.gradle.BaseExtension
import uk.gov.authentication.ext.BaseExtensions.baseAndroidConfig
import uk.gov.authentication.ext.LintExtensions.configureLintOptions

listOf(
    "com.android.library",
    "kotlin-kapt",
    "org.jetbrains.kotlin.android",
    "uk.gov.authentication.detekt-config",
    "uk.gov.authentication.emulator-config",
    "uk.gov.authentication.jacoco-module-config",
    "uk.gov.authentication.jvm-toolchains",
    "uk.gov.authentication.ktlint-config",
    "uk.gov.authentication.sonarqube-module-config",
).forEach {
    project.plugins.apply(it)
}

configure<BaseExtension> {
    baseAndroidConfig(project)
}

configure<LibraryExtension> {
    lint(configureLintOptions("${rootProject.projectDir}/config"))
}
