import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")

    id("org.jlleitschuh.gradle.ktlint") version "10.3.0" apply false
    id("io.gitlab.arturbosch.detekt") version "1.20.0" apply false
}

group = "org.geogebra.solver"
version = "0.1"

subprojects {

    // It would be better to handle this in a "conventions plugin" (see
    // https://docs.gradle.org/current/samples/sample_convention_plugins.html). However I have not been able to make
    // that work.
    val jvmTarget: String by project
    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = jvmTarget
    }
}
