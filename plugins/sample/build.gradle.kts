plugins {
    alias(libs.plugins.kotlin.jvm)
}

val pluginVersion: String by project
version = pluginVersion
group = "com.pissiphany.bany"

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    api(project(":bany-plugin-api"))
}
