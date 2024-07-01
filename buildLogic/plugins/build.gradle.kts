plugins {
    `kotlin-dsl`
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

dependencies {
    listOf(
        libs.android.build.tool,
        libs.detekt.gradle,
        libs.kotlin.gradle.plugin,
        libs.ktlint.gradle,
        libs.sonarqube.gradle,
    ).forEach { dependency ->
        implementation(dependency)
    }
}

kotlin { jvmToolchain(17) }

ktlint {
    filter {
        exclude { it.file.absolutePath.contains("/build/") }
    }
}
