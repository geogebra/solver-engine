/*
 * Copyright (c) 2024 GeoGebra GmbH, office@geogebra.org
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask
import org.jlleitschuh.gradle.ktlint.tasks.KtLintFormatTask

plugins {
    kotlin("jvm")
    antlr
    kotlin("plugin.serialization")

    id("io.gitlab.arturbosch.detekt")
    id("java-test-fixtures")
    id("com.google.devtools.ksp")
}

dependencies {
    antlr("org.antlr:antlr4:4.12.0")

    testFixturesImplementation(kotlin("test"))
    testImplementation(kotlin("test"))
    testFixturesImplementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    testFixturesImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    testImplementation("net.javacrumbs.json-unit:json-unit-assertj:2.38.0")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")

    ksp(project(":export"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    // The antlr plugin defines a dependency for java compilation but not Kotlin, so we do it manually here.
    dependsOn(tasks.withType<AntlrTask>())
}

tasks.withType<KtLintCheckTask> {
    // It looks like gradle cannot figure out that ktlint doesn't depend on the antlr task and complains if we don't
    // declare this dependency
    mustRunAfter(tasks.withType<AntlrTask>())
}

tasks.withType<KtLintFormatTask> {
    // It looks like gradle cannot figure out that ktlint doesn't depend on the antlr task and complains if we don't
    // declare this dependency
    mustRunAfter(tasks.withType<AntlrTask>())
}

tasks.generateGrammarSource {
    arguments = arguments + listOf("-visitor")
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("$rootDir/config/detekt.yaml"))
}
