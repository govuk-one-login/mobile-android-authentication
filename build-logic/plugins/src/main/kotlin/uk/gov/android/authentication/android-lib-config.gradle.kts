package uk.gov.android.authentication

import com.android.build.api.dsl.LibraryExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

//https://github.com/gradle/gradle/issues/15383
val libs = the<LibrariesForLibs>()

plugins.apply("uk.gov.pipelines.android-lib-config")
plugins.apply("uk.gov.android.authentication.android-test-config")

configure<LibraryExtension> {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

configure<KotlinAndroidProjectExtension> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}
