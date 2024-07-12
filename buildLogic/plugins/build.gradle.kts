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

gradlePlugin {
    plugins {
        register("uk.gov.sonar.module-config") {
            description = """
                Configures sonar for a gradle sub-module.
                Uses 'SonarModuleConfigExtension' as a feature toggle.
            """.trimIndent()
            id = this.name
            implementationClass = "uk.gov.sonar.SonarModuleConfigPlugin"
        }
    }
}

kotlin { jvmToolchain(17) }

ktlint {
    filter {
        exclude { it.file.absolutePath.contains("/build/") }
    }
}
