import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application

    val kotlinVersion = "1.7.0"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion

    antlr

    id("org.openapi.generator") version "6.0.0"
    id("org.springframework.boot") version "2.7.0"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"

    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
    id("io.gitlab.arturbosch.detekt") version "1.20.0"
}

group = "org.geogebra.solver"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    antlr("org.antlr:antlr4:4.10.1")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("jakarta.validation:jakarta.validation-api")
    implementation("jakarta.annotation:jakarta.annotation-api:2.1.1")
    implementation("org.springdoc:springdoc-openapi-data-rest:1.6.9")
    implementation("org.springdoc:springdoc-openapi-ui:1.6.9")
    implementation("org.springdoc:springdoc-openapi-kotlin:1.6.9")
    implementation("javax.annotation:javax.annotation-api:1.3.2")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"

    // The antlr plugin defines a dependency for java compilation but not Kotlin, so we do it manually here.
    dependsOn(tasks.generateGrammarSource)

    dependsOn(tasks.openApiGenerate)
}

tasks.generateGrammarSource {
    arguments = arguments + listOf("-visitor")
}

detekt {
    buildUponDefaultConfig = true
    config = files("$projectDir/config/detekt.yaml")
}

application {
    mainClass.set("org.openapitools.ApplicationKt")
}

tasks.openApiGenerate {
    generatorName.set("kotlin-spring")
    inputSpec.set("$projectDir/src/main/openapi/api-specification.yaml")
    packageName.set("server.application")
    apiPackage.set("server.api")
    modelPackage.set("server.models")
    outputDir.set("$buildDir/generated-src/openapi")
    configOptions.set(
        mapOf(
            "serviceInterface" to "true",
            "basePackage" to "server"
        )
    )
}

sourceSets["main"].java {
    srcDirs("$buildDir/generated-src/openapi/src/main/kotlin")
}
