// Based on:
// https://github.com/gradle/kotlin-dsl/blob/master/samples/multi-kotlin-project/build.gradle.kts
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.21" apply false
}

allprojects {
    group = "com.pissiphany.bany"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}

//dependencies {
//    subprojects.forEach {
//        archives(it)
//    }
//}
