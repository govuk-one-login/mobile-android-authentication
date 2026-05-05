package uk.gov.android.authentication

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.kotlin.dsl.kotlin

//https://github.com/gradle/gradle/issues/15383
val libs = the<LibrariesForLibs>()

dependencies {
    "testImplementation"(kotlin("test"))
    "testImplementation"(kotlin("test-junit5"))
    "testImplementation"(libs.bundles.test)
    "testImplementation"(libs.junit)
    "testImplementation"(libs.mockito.kotlin)
    "testImplementation"(platform(libs.junit.bom))

    "testRuntimeOnly"(libs.junit.vintage.engine)
}
