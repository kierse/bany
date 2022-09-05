plugins {
    kotlin("jvm")
    kotlin("kapt")
}

val pluginVersion: String by project
version = pluginVersion
group = "com.pissiphany.bany"

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    api(project(":shared"))
    api(project(":bany-plugin-api"))

    implementation(libs.square.okhttp)
    implementation(libs.square.moshi)
    kapt(libs.square.moshi.codegen)

    // pf4j
    implementation(libs.pf4j)
    kapt(libs.pf4j)

    kaptTest(libs.square.moshi.codegen)
    testImplementation(libs.square.okhttp.mock.webserver)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.jupiter.params)
}

tasks.named<Test>("test") {
    sourceSets {
        test {
            // Append "-PincludeIntegration" to command line to run instrumentation
            if (project.hasProperty("includeIntegration")) {
                println("Running integration tests...")
            } else {
                exclude("**/*Integration*.class")
            }
        }
    }

    useJUnitPlatform()
}
