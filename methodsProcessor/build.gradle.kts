plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":engine"))
    implementation(project(":export"))
    implementation(libs.ksp.api)
}
