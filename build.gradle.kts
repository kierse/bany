import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.4.21"
    kotlin("kapt") version "1.4.21"
}

// https://docs.gradle.org/current/userguide/application_plugin.html
application {
    mainClass.set("com.pissiphany.bany.BanyKt")
}

group = "com.pissiphany.bany"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "14"
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(project(":bany-plugin-api"))
    implementation(project(":domain"))
    implementation(project(":adapter"))
    implementation(project(":plugins:equitable"))

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.moshi:moshi:1.11.0")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.11.0")

    // pf4j
    implementation("org.pf4j:pf4j:3.4.1")
    implementation("org.slf4j:slf4j-simple:1.7.30")

    // TODO needed?
    kaptTest("com.squareup.moshi:moshi-kotlin-codegen:1.11.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
}

tasks.named<Test>("test") {
    sourceSets {
        test {
            // Append "-PincludeIntegration" to command line to run instrumentation
            if (!project.hasProperty("includeIntegration")) {
                exclude("**/*Integration*.class")
            }
        }
    }

    useJUnitPlatform()
}
