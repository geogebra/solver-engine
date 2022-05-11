import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.21"
    application
    antlr
}

group = "org.example"
version = "1.0-SNAPSHOT"

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

// Arnaud: This should generate the grammar java source in the correct place,
// but I can't make IDEA pick up the generated source for some reason.
tasks.generateGrammarSource {
    arguments = arguments + listOf("-visitor")
}

application {
    mainClass.set("MainKt")
}
