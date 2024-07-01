package uk.gov.authentication.ext

import com.android.build.api.dsl.Lint
import java.io.File

object LintExtensions {
    fun configureLintOptions(configDir: String): Lint.() -> Unit =
        {
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
}
