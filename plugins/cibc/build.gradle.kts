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

    // pf4j
    implementation("org.pf4j:pf4j:2.6.0")
    kapt("org.pf4j:pf4j:2.6.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.4.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.0")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

