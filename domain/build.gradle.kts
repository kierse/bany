plugins {
    kotlin("jvm")
}

group = "com.pissiphany.bany"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    testImplementation(Versions.Junit.Jupiter.dependency)
    testImplementation(Versions.Junit.Jupiter.Api.dependency)
    testRuntimeOnly(Versions.Junit.Jupiter.Engine.dependency)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
