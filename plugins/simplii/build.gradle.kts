plugins {
    kotlin("jvm")
    kotlin("kapt")
}

val pluginVersion: String by project
version = pluginVersion
group = "com.pissiphany.bany"

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(project(":bany-plugin-api"))

    // pf4j
    implementation("org.pf4j:pf4j:2.6.0")
    kapt("org.pf4j:pf4j:2.6.0")
}

