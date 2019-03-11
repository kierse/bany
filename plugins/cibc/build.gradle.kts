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

    implementation("com.squareup.okhttp3:okhttp:3.13.1")
    implementation("com.squareup.moshi:moshi:1.8.0")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.8.0")

    // pf4j
    implementation("org.pf4j:pf4j:2.6.0")
    kapt("org.pf4j:pf4j:2.6.0")
}

