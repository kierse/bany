plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
}

val pluginVersion: String by project
version = pluginVersion
group = "com.pissiphany.bany"

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    api(project(":shared"))
    api(project(":bany-plugin-api"))

    implementation(libs.square.okhttp)
    implementation(libs.jsoup)

    kaptTest(libs.square.moshi.codegen)
    testImplementation(libs.square.moshi)
    testImplementation(libs.square.okhttp.mock.webserver)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
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
