plugins {
    kotlin("jvm")
    kotlin("kapt")
}

val pluginVersion: String by project
version = pluginVersion
group = "com.pissiphany.bany"

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    api(project(":bany-plugin-api"))

    // pf4j
    implementation("org.pf4j:pf4j:3.4.1")
    kapt("org.pf4j:pf4j:3.4.1")
}
