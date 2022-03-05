import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version Versions.kotlin
    kotlin("kapt") version Versions.kotlin
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
    implementation(project(":plugins:crypto"))
    implementation(project(":plugins:equitable"))
    implementation(project(":plugins:stock"))

    implementation(Versions.KotlinX.Coroutines.Core.dependency)

    implementation(Versions.Square.Retrofit.dependency)
    implementation(Versions.Square.Retrofit.Converters.Moshi.dependency)
    implementation(Versions.Square.Moshi.dependency)
    kapt(Versions.Square.Moshi.KotlinCodegen.dependency)

    implementation(Versions.KotlinLogging.dependency)

    // pf4j
    implementation(Versions.Pf4j.dependency)
    implementation(Versions.Slf4j.dependency)

    kaptTest(Versions.Square.Moshi.KotlinCodegen.dependency)
    testImplementation(Versions.KotlinX.Coroutines.Test.dependency)
    testImplementation(Versions.Square.OkHttp.MockWebServer.dependency)
    testImplementation(Versions.Square.Retrofit.Converters.Scalars.dependency)
    testImplementation(Versions.Junit.Jupiter.dependency)
    testImplementation(Versions.Junit.Jupiter.Api.dependency)
    testRuntimeOnly(Versions.Junit.Jupiter.Engine.dependency)
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
