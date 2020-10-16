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
    implementation("com.squareup.moshi:moshi:1.11.0")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.11.0")

    // pf4j
    implementation("org.pf4j:pf4j:3.4.1")
    kapt("org.pf4j:pf4j:3.4.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.7.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

