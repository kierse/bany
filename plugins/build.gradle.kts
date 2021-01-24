// based on: https://github.com/pf4j/pf4j/blob/master/demo_gradle/plugins/build.gradle
plugins {
    kotlin("jvm")
    `java-library-distribution`
}

subprojects
    .filter { it.hasProperty("pluginId") }
    .forEach { project ->
        // Note: need to apply the plugin here so we have (and can use) the Jar task
        project.apply(plugin = "kotlin")

        project.tasks.jar {
            manifest {
                // make sure each plugin jar manifest is properly populated
                attributes(
                    mapOf(
                        "Plugin-Class" to "${project.findProperty("pluginClass")}",
                        "Plugin-Id" to "${project.findProperty("pluginId")}",
                        "Plugin-Version" to "${project.findProperty("pluginVersion")}",
                        "Plugin-Provider" to "${project.findProperty("pluginProvider")}"
                    )
                )
            }
        }
    }

