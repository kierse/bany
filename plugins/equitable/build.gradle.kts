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

    implementation("org.jsoup:jsoup:1.13.1")

    // pf4j
    implementation("org.pf4j:pf4j:3.4.1")
    kapt("org.pf4j:pf4j:3.4.1")

    testImplementation("com.squareup.moshi:moshi:1.11.0")
    kaptTest("com.squareup.moshi:moshi-kotlin-codegen:1.11.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
}

tasks.named<Test>("test") {
    sourceSets {
        test {
            if (!project.hasProperty("includeIntegration")) {
                exclude("**/*Integration*.class")
            }
        }
    }

    useJUnitPlatform()
}
