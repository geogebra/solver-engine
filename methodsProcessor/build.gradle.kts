plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":export"))
    implementation(libs.ksp.api)
}
