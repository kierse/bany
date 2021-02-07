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
    implementation(Versions.Pf4j.dependency)
    kapt(Versions.Pf4j.dependency)
}
