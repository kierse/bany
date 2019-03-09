plugins {
    kotlin("jvm")
}

val pluginVersion: String by project
version = pluginVersion
group = "com.pissiphany.bany"

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(project(":plugins:bany-plugin-api"))

    // pf4j
    implementation("org.pf4j:pf4j:2.6.0")
}
