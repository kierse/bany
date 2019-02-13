plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(project(":adapter"))
    implementation(project(":domain"))

    implementation("com.squareup.retrofit2:retrofit:2.5.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.4.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.0")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
