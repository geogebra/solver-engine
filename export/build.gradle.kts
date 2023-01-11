plugins {
    kotlin("jvm")
}

dependencies {
    implementation(libs.ksp.api)
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.3")
    implementation("com.google.devtools.ksp:symbol-processing-api:1.7.10-1.0.6")
}
