import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
    application

    kotlin("jvm")
    kotlin("plugin.jpa")
    kotlin("plugin.spring")

    id("org.openapi.generator") version "6.2.1"
    id("org.springframework.boot") version "2.7.7"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"

    id("io.gitlab.arturbosch.detekt")
}

group = "org.geogebra.solver"
version = "0.1"

dependencies {
    implementation(project(":engine"))
    implementation(project(":methods"))

    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    implementation("jakarta.validation:jakarta.validation-api")
    implementation("jakarta.annotation:jakarta.annotation-api:2.1.1")
    implementation("org.springdoc:springdoc-openapi-data-rest:1.6.12")
    implementation("org.springdoc:springdoc-openapi-ui:1.6.12")
    implementation("org.springdoc:springdoc-openapi-kotlin:1.6.12")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}

configurations.implementation {
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
}

tasks.withType<KotlinCompile> {
    dependsOn(tasks.openApiGenerate)
}

application {
    mainClass.set("server.ApplicationKt")
}

val generatedRoot: String = File(buildDir, "generated-src/openapi").absolutePath

tasks.openApiGenerate {
    generatorName.set("kotlin-spring")
    // currently we use our own generators to allow async request handling
    templateDir.set("$projectDir/src/main/resources/generator-template")
    inputSpec.set("$projectDir/src/main/openapi/api-specification.yaml")
    packageName.set("server.application")
    apiPackage.set("server.api")
    modelPackage.set("server.models")
    outputDir.set(generatedRoot)
    configOptions.set(
        mapOf(
            "serviceInterface" to "true",
            "basePackage" to "server",
            "enumPropertyNaming" to "PascalCase",
        ),
    )
}

tasks.named<BootBuildImage>("bootBuildImage") {
    imageName = "registry.git.geogebra.org/solver-team/solver-engine/${project.name}:${project.version}"
    isPublish = true
    docker {
        publishRegistry {
            username = System.getenv("CI_REGISTRY_USER")
            password = System.getenv("CI_REGISTRY_PASSWORD")
            url = "https://registry.git.geogebra.org/v2/"
        }
    }
}

sourceSets["main"].java {
    srcDirs("$generatedRoot/src/main/kotlin")
}

detekt {
    buildUponDefaultConfig = true
    config = files("$rootDir/config/detekt.yaml")
}

ktlint {
    filter {
        exclude {
            it.file.path.contains(generatedRoot)
        }
    }
}

tasks.withType<KtLintCheckTask> {
    // It looks like gradle cannot figure out that ktlint doesn't depend on openApiGenerate and complains if we don't
    // declare this dependency
    mustRunAfter(tasks.openApiGenerate)
}
