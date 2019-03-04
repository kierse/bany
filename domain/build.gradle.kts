plugins {
    kotlin("jvm")
}

group = "com.pissiphany.bany.domain"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    testImplementation("org.junit.jupiter:junit-jupiter:5.4.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.0")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
