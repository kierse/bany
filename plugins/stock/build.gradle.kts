plugins {
    kotlin("jvm")
    kotlin("kapt")
}

val pluginVersion: String by project
version = pluginVersion
group = "com.pissiphany.bany.plugin.stock"

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    api(project(":shared"))
    api(project(":bany-plugin-api"))

    implementation(Versions.Square.OkHttp.dependency)
    implementation(Versions.Square.Moshi.dependency)
    kapt(Versions.Square.Moshi.KotlinCodegen.dependency)

    // pf4j
    implementation(Versions.Pf4j.dependency)
    kapt(Versions.Pf4j.dependency)
}
