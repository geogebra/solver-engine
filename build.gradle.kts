import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.21"
    application
    antlr
}

group = "org.geogebra.solver"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.2")
    antlr("org.antlr:antlr4:4.10.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "16"

    // The antlr plugin defines a dependency for java compilation but not Kotlin, so we do it manually here.
    dependsOn(tasks.generateGrammarSource)
}

tasks.compileTestKotlin {

    // The antlr plugin defines a dependency for java compilation but not Kotlin, so we do it manually here.
    dependsOn(tasks.generateTestGrammarSource)
}

tasks.generateGrammarSource {
    arguments = arguments + listOf("-visitor")
}

application {
    mainClass.set("MainKt")
}
