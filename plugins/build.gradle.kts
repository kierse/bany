// based on: https://github.com/pf4j/pf4j/blob/master/demo_gradle/plugins/build.gradle
plugins {
    kotlin("jvm")
}

subprojects
    .filter { it.hasProperty("pluginId") }
    .forEach { project ->
        // Note: need to apply the plugin here so we have (and can use) the Jar task
        project.apply(plugin = "kotlin")

        val jar by project.tasks.existing(Jar::class)
        project.tasks.register<Copy>("copyJar") {
            val pluginsDir: String by rootProject.extra
            from(jar)
            into(pluginsDir)
        }
    }

tasks.named("build") {
    // make "build" task depend on all sub-project custom tasks named "assemblePlugin"
    subprojects.forEach { dependsOn("${it.name}:copyJar") }
}
