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

    implementation(Versions.Square.OkHttp.dependency)
    implementation(Versions.Square.Moshi.dependency)
    kapt(Versions.Square.Moshi.KotlinCodegen.dependency)

    // pf4j
    implementation(Versions.Pf4j.dependency)
    kapt(Versions.Pf4j.dependency)

    kaptTest(Versions.Square.Moshi.KotlinCodegen.dependency)
    testImplementation(Versions.Square.OkHttp.MockWebServer.dependency)
    testImplementation(Versions.Junit.Jupiter.dependency)
    testImplementation(Versions.Junit.Jupiter.Api.dependency)
    testRuntimeOnly(Versions.Junit.Jupiter.Engine.dependency)
    testRuntimeOnly(Versions.Junit.Jupiter.Params.dependency)
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
