import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.21"
    kotlin("kapt") version "1.3.21"
}

group = "com.pissiphany.bany"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()

//        flatDir {
//            // so all projects can find bany-plugin jar
//            dirs(rootProject.file("lib").absolutePath)
//        }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}

// plugin location
rootProject.extra["pluginsDir"] = "${rootProject.buildDir.path}/plugins"

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(project(":bany-plugin-api"))
    implementation(project(":domain"))
    implementation(project(":adapter"))

    implementation("com.squareup.retrofit2:retrofit:2.5.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.5.0")
    implementation("com.squareup.moshi:moshi:1.8.0")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.8.0")

    // pf4j
    implementation("org.pf4j:pf4j:2.6.0")
    implementation("org.slf4j:slf4j-simple:1.7.26")

    testImplementation("org.junit.jupiter:junit-jupiter:5.4.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.0")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
