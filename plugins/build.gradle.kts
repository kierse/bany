// based on: https://github.com/pf4j/pf4j/blob/master/demo_gradle/plugins/build.gradle
plugins {
    kotlin("jvm")
}

subprojects
    .filter { it.hasProperty("pluginId") }
    .forEach { project ->
        // Note: need to apply the plugin here so we have (and can use) the Jar task
        project.apply(plugin = "kotlin")

        val jar by project.tasks.existing(Jar::class) {
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

        project.tasks.register<Copy>("assemblePlugin") {
            val pluginsDir: String by rootProject.extra
            from(jar)
            into(pluginsDir)
        }
    }

tasks.named("build") {
    // make "build" task depend on all sub-project custom tasks named "assemblePlugin"
    subprojects.forEach { dependsOn("${it.name}:assemblePlugin") }
}
