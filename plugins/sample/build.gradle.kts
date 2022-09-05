plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
}

val pluginVersion: String by project
version = pluginVersion
group = "com.pissiphany.bany"

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    api(project(":bany-plugin-api"))

    // pf4j
    implementation(libs.pf4j)
    kapt(libs.pf4j)
}
