plugins {
    kotlin("jvm")
}

group = "com.pissiphany.bany"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.pf4j:pf4j:2.6.0")
}

//val jar: Jar by tasks
//jar.apply {
//    manifest {
//        attributes(mapOf("Implementation-Title" to project.name, "Implementation-Version" to project.version))
//    }
//}