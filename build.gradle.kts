plugins {
    val kotlinVersion = "1.7.10"
    kotlin("jvm") version kotlinVersion

    id("org.jlleitschuh.gradle.ktlint") version "10.3.0" apply false
    id("io.gitlab.arturbosch.detekt") version "1.20.0" apply false
}

group = "org.geogebra.solver"
version = "0.1"

repositories {
    mavenCentral()
}
