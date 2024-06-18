import org.sonarqube.gradle.SonarExtension
import uk.gov.authentication.Filters
import uk.gov.authentication.SourceSetFolder

plugins {
    id("org.sonarqube")
}

fun generateCommaSeparatedFiles(iterator: Iterable<String>) =
    fileTree(project.projectDir) {
        this.setIncludes(iterator)
    }.files.joinToString(
        separator = ",",
        transform = File::getAbsolutePath,
    )

val androidLintReportFiles by project.extra(
    generateCommaSeparatedFiles(listOf("**/reports/lint-results-*.xml")),
)
val detektReportFiles by project.extra(
    generateCommaSeparatedFiles(listOf("**/reports/detekt/*.xml")),
)
val jacocoXmlReportFiles by project.extra(
    generateCommaSeparatedFiles(
        listOf(
            // android instrumentation test reports:
            "**/reports/coverage/**/*.xml",
            // unit test reports:
            "**/reports/jacoco/**/*.xml",
        ),
    ),
)
val junitReportFiles by project.extra(
    generateCommaSeparatedFiles(
        listOf(
            // instrumentation:
            "**/outputs/androidTest-results/connected/flavors/*/",
            // unit tests:
            "**/test-results",
        ),
    ),
)
val ktLintReportFiles by project.extra(
    generateCommaSeparatedFiles(listOf("**/reports/ktlint/**/*.xml")),
)
val sonarExclusions by project.extra(
    listOf(
        Filters.androidInstrumentationTests,
        Filters.sonar,
        Filters.testSourceSets,
    ).flatten().joinToString(separator = ","),
)

val moduleSourceFolder = SourceSetFolder(project)
var sourceFolders by project.extra("")
var testFolders by project.extra("")

var projectSonarProperties by project.extra(
    mapOf<String, Any>(),
)

configure<SonarExtension> {
    if (moduleSourceFolder.srcExists()) {
        sourceFolders = moduleSourceFolder.sourceFolders
        testFolders = moduleSourceFolder.testFolders
    }

    projectSonarProperties =
        mapOf<String, Any>(
            "sonar.sources" to sourceFolders,
            "sonar.tests" to testFolders,
            "sonar.exclusions" to sonarExclusions,
            "sonar.androidLint.reportPaths" to androidLintReportFiles,
            "sonar.coverage.jacoco.xmlReportPaths" to jacocoXmlReportFiles,
            "sonar.kotlin.detekt.reportPaths" to detektReportFiles,
            "sonar.kotlin.ktlint.reportPaths" to ktLintReportFiles,
            "sonar.junit.reportPaths" to junitReportFiles,
        )

    properties {
        projectSonarProperties.forEach { (key: String, value: Any) ->
            property(key, value)
        }
    }
}
