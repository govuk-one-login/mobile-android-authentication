import io.gitlab.arturbosch.detekt.extensions.DetektExtension

project.plugins.apply("io.gitlab.arturbosch.detekt")

configure<DetektExtension>(setupDetekt())

fun setupDetekt(): DetektExtension.() -> Unit =
    {
        val configDir = "${project.rootProject.projectDir}/config"
        logger.info("Detekt configuration: Using config directory: $configDir")

        val detektToolVersion = "1.23.1"

        allRules = true
        buildUponDefaultConfig = true
        config.from(file("$configDir/detekt/detektConfig.yml"))
        debug = false
        disableDefaultRuleSets = false
        ignoreFailures = false
        parallel = true
        source.from(
            fileTree("src") {
                include(
                    listOf("**/java/**/*.*"),
                )
            },
        )
        toolVersion = detektToolVersion
    }
