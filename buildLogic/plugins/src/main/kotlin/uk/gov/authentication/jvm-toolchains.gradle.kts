package uk.gov.authentication

import com.android.build.gradle.BaseExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.extra
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

val jvmVersion by extra(11)

plugins {
    id("kotlin-android")
}

configure<JavaPluginExtension> {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(jvmVersion))
    }
}

configure<KotlinAndroidProjectExtension> {
    jvmToolchain(jvmVersion)
}

configure<BaseExtension> {
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(jvmVersion)
        targetCompatibility = JavaVersion.toVersion(jvmVersion)
    }
}
