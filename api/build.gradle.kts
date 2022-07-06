import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application

    val kotlinVersion = "1.7.0"
    kotlin("jvm")
    kotlin("plugin.jpa") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion

    id("org.openapi.generator") version "6.0.0"
    id("org.springframework.boot") version "2.7.0"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"

    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
}

group = "org.geogebra.solver"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":engine"))
    implementation(project(":methods"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("jakarta.validation:jakarta.validation-api")
    implementation("jakarta.annotation:jakarta.annotation-api:2.1.1")
    implementation("org.springdoc:springdoc-openapi-data-rest:1.6.9")
    implementation("org.springdoc:springdoc-openapi-ui:1.6.9")
    implementation("org.springdoc:springdoc-openapi-kotlin:1.6.9")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"

    dependsOn(tasks.openApiGenerate)
}

application {
    mainClass.set("server.ApplicationKt")
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
            "basePackage" to "server",
            "enumPropertyNaming" to "PascalCase",
        )
    )
}

sourceSets["main"].java {
    srcDirs("$buildDir/generated-src/openapi/src/main/kotlin")
}

detekt {
    buildUponDefaultConfig = true
    config = files("$rootDir/config/detekt.yaml")
}
