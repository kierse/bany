import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
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
        kotlinOptions.jvmTarget = "18"
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    api(project(":shared"))
    implementation(project(":bany-plugin-api"))
    implementation(project(":config-api"))
    implementation(project(":domain"))
    implementation(project(":adapter"))
    implementation(project(":plugins:crypto"))
    implementation(project(":plugins:equitable"))
    implementation(project(":plugins:stock"))

    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.square.okhttp.interceptor.logging)
    implementation(libs.square.retrofit)
    implementation(libs.square.retrofit.converters.moshi)
    implementation(libs.square.moshi)
    kapt(libs.square.moshi.codegen)

    implementation(libs.kotlin.logging)

    implementation(libs.slf4j)

    kaptTest(libs.square.moshi.codegen)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.square.okhttp.mock.webserver)
    testImplementation(libs.square.retrofit.converters.scalars)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.named<Test>("test") {
    sourceSets {
        test {
            // Append "-PincludeIntegration" to command line to run instrumentation
            if (project.hasProperty("includeIntegration")) {
                println("Running integration tests...")
            } else {
                exclude("**/*Integration*.class")
            }
        }
    }

    useJUnitPlatform()
}
