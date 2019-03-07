plugins {
    kotlin("jvm")
}

group = "com.pissiphany.bany.plugin.sample"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(project(":domain"))

//    implementation("com.squareup.retrofit2:retrofit:2.5.0")
//    implementation("com.squareup.retrofit2:converter-moshi:2.5.0")
//    implementation("com.squareup.moshi:moshi:1.8.0")
//    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.8.0")
//
//    // pf4j
//    implementation("org.pf4j:pf4j:2.6.0")
//    implementation("com.pissiphany.bany.adapter.plugin:bany-plugin:1.0-SNAPSHOT")

    testImplementation("org.junit.jupiter:junit-jupiter:5.4.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.0")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
