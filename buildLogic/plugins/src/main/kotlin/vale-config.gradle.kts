val valeSync =
    rootProject.tasks.register("valeSync", Exec::class.java) {
        description = "Lint the project's markdown and text files with Vale."
        group = "verification"
        executable = "vale"
        setArgs(
            listOf("sync"),
        )
    }

val vale =
    rootProject.tasks.register("vale", Exec::class.java) {
        description = "Lint the project's markdown and text files with Vale."
        group = "verification"
        executable = "vale"
        dependsOn(valeSync)
        setArgs(
            listOf(
                "--no-wrap",
                "--config=${rootProject.projectDir}/.vale.ini",
                rootProject.projectDir.toString(),
            ),
        )
    }

val check =
    rootProject.tasks.maybeCreate(
        "check",
    )
        .apply {
            dependsOn("vale")
        }
