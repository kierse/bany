plugins {
    kotlin("jvm")
}

group = "com.pissiphany.bany"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    api(Versions.KotlinLogging.dependency)
    implementation(Versions.Slf4j.dependency)

    implementation(Versions.Square.OkHttp.dependency)

    testImplementation(Versions.Square.OkHttp.MockWebServer.dependency)
    testImplementation(Versions.KotlinX.Coroutines.Test.dependency)
    testImplementation(Versions.Junit.Jupiter.dependency)
    testImplementation(Versions.Junit.Jupiter.Api.dependency)
    testRuntimeOnly(Versions.Junit.Jupiter.Engine.dependency)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
