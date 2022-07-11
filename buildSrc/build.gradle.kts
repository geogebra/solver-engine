import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.3")
    implementation("com.pinterest.ktlint:ktlint-core:0.46.1")
    implementation("com.pinterest.ktlint:ktlint-ruleset-standard:0.46.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}
