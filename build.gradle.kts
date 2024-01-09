/*
 * Copyright (c) 2024 GeoGebra GmbH, office@geogebra.org
 */

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension

val ktlintVersion: String by project

plugins {
    kotlin("jvm")

    id("org.jlleitschuh.gradle.ktlint") apply false
    id("io.gitlab.arturbosch.detekt") apply false
}

group = "org.geogebra.solver"
version = "0.1"

subprojects {

    // It would be better to handle this in a "conventions plugin" (see
    // https://docs.gradle.org/current/samples/sample_convention_plugins.html). However I have not been able to make
    // that work.
    apply(plugin = "kotlin")
    configure<KotlinJvmProjectExtension> {
        jvmToolchain(21)
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.allWarningsAsErrors = false
    }

    // The IntelliJ ktlint plugin enforces a specific version of ktlint so we stick with that version in order to get
    // consistent results from IDE and gradle.
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    configure<KtlintExtension> {
        version.set(ktlintVersion)
    }

    // According to the detekt documentation and the example provided in the README of the project, the Java Target
    // Level should be set to 1.8 as follows, although it seems to work without this configuration.
    tasks.withType<Detekt>().configureEach {
        jvmTarget = "1.8"
    }
    tasks.withType<DetektCreateBaselineTask>().configureEach {
        jvmTarget = "1.8"
    }
}
