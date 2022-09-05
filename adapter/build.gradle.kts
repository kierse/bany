plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "com.pissiphany.bany"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(project(":shared"))
    implementation(project(":bany-plugin-api"))
    implementation(project(":domain"))

    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
