/*
 * Copyright (c) 2024 GeoGebra GmbH, office@geogebra.org
 */

plugins {
    kotlin("jvm")
}

dependencies {
    implementation(libs.ksp.api)
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.14.2")
    implementation("com.google.devtools.ksp:symbol-processing-api:1.9.22-1.0.16")
}
