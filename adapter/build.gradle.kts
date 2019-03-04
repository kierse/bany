plugins {
    kotlin("jvm") //version "1.3.21"
    kotlin("kapt") //version "1.3.21"
}

group = "com.pissiphany.bany.adapter"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(project(":domain"))

    implementation("com.squareup.retrofit2:retrofit:2.5.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.5.0")
    implementation("com.squareup.moshi:moshi:1.8.0")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.8.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.4.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.0")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
