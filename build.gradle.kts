import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.0"
    application
    antlr
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
    id("io.gitlab.arturbosch.detekt") version "1.20.0"
}

group = "org.geogebra.solver"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.jr:jackson-jr-objects:2.13.3")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.2")
    testImplementation("net.pwall.json:json-kotlin-schema:0.34")
    antlr("org.antlr:antlr4:4.10.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"

    // The antlr plugin defines a dependency for java compilation but not Kotlin, so we do it manually here.
    dependsOn(tasks.generateGrammarSource)
}

tasks.generateGrammarSource {
    arguments = arguments + listOf("-visitor")
}

detekt {
    buildUponDefaultConfig = true
    config = files("$projectDir/config/detekt.yaml")
}

application {
    mainClass.set("MainKt")
}
