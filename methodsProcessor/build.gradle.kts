/*
 * Copyright (c) 2024 GeoGebra GmbH, office@geogebra.org
 */

plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":engine"))
    implementation(project(":export"))
    implementation(libs.ksp.api)
}
