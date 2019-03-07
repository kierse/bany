plugins {
    kotlin("jvm")
}

group = "com.pissiphany.bany"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(project(":plugins:api"))

    // pf4j
    implementation("org.pf4j:pf4j:2.6.0")
}
