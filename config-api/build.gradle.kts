plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "com.pissiphany.bany"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(libs.square.moshi)
}
