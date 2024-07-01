package uk.gov.authentication.ext

import com.android.build.gradle.api.LibraryVariant
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized
import uk.gov.authentication.filetree.fetcher.AsmFileTreeFetcher
import uk.gov.authentication.filetree.fetcher.FileTreesFetcher
import uk.gov.authentication.filetree.fetcher.JavaCompileFileTreeFetcher
import uk.gov.authentication.filetree.fetcher.KotlinCompileFileTreeFetcher
import uk.gov.authentication.jacoco.tasks.JacocoCombinedTestTaskGenerator
import uk.gov.authentication.jacoco.tasks.JacocoConnectedTestTaskGenerator
import uk.gov.authentication.jacoco.tasks.JacocoTaskGenerator
import uk.gov.authentication.jacoco.tasks.JacocoUnitTestTaskGenerator

fun LibraryVariant.generateDebugJacocoTasks(project: Project) {
    val capitalisedFlavorName = flavorName.capitalized()
    if (buildType.name == "debug") {
        val classDirectoriesFetcher =
            FileTreesFetcher(
                project,
                KotlinCompileFileTreeFetcher(
                    project,
                    name,
                    capitalisedFlavorName,
                ),
                AsmFileTreeFetcher(
                    project,
                    name,
                    capitalisedFlavorName,
                ),
                JavaCompileFileTreeFetcher(
                    project,
                    name,
                    capitalisedFlavorName,
                ),
            )

        val unitTestReportGenerator =
            JacocoUnitTestTaskGenerator(
                project,
                classDirectoriesFetcher,
                name,
            )

        val connectedTestReportGenerator =
            JacocoConnectedTestTaskGenerator(
                project,
                classDirectoriesFetcher,
                name,
            )

        val combinedTestReportGenerator =
            JacocoCombinedTestTaskGenerator(
                project = project,
                classDirectoriesFetcher = classDirectoriesFetcher,
                variant = name,
                configurations =
                    listOf(
                        unitTestReportGenerator,
                        connectedTestReportGenerator,
                    ),
            )

        listOf(
            unitTestReportGenerator,
            connectedTestReportGenerator,
            combinedTestReportGenerator,
        ).forEach(JacocoTaskGenerator::customTask)
    }
}
