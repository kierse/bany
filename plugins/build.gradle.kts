// based on: https://github.com/pf4j/pf4j/blob/master/demo_gradle/plugins/build.gradle
subprojects {
    apply(plugin = "kotlin")

    val jar by tasks.existing(Jar::class) {
        manifest {
            val pluginClass: String by project
            val pluginId: String by project
            val pluginVersion: String by project
            val pluginProvider: String by project

            attributes(
                mapOf(
                    "Plugin-Class" to pluginClass,
                    "Plugin-Id" to pluginId,
                    "Plugin-Version" to pluginVersion,
                    "Plugin-Provider" to pluginProvider
                )
            )
        }
    }

    tasks.register<Copy>("assemblePlugin") {
        val pluginsDir: String by rootProject.extra
        from(jar)
        into(pluginsDir)
    }
}

tasks.register<Copy>("pluginConfiguration") {
    val pluginsDir: String by rootProject.extra

    // copy disabled.txt into pluginsDir
    from("${projectDir.absolutePath}/disabled.txt")
    into(pluginsDir)

    // copy enabled.txt into pluginsDir
    from("${projectDir.absolutePath}/enabled.txt")
    into(pluginsDir)
}

tasks.named("build") {
    // make "build" task depend on custom task named "pluginConfiguration"
    dependsOn(project.tasks["pluginConfiguration"])

    // make "build" task depend on all sub-project custom tasks named "assemblePlugin"
    subprojects.forEach { dependsOn("${it.name}:assemblePlugin") }
}
