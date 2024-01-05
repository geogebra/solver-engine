import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
    application

    kotlin("jvm")
    kotlin("plugin.jpa")
    kotlin("plugin.spring")

    // We use the kotlin-spring generator but its mustache template files are customized.  This means that when the
    // version of openapi-generator is bumped, template files for kotlin-spring need to be fetched again from the
    // repository.  See src/main/resources/openapi-generator-7.2.0/NOTE.md for more details.
    id("org.openapi.generator") version "7.2.0"
    id("org.springframework.boot") version "3.2.1"
    id("io.spring.dependency-management") version "1.1.4"

    id("io.gitlab.arturbosch.detekt")
}

group = "org.geogebra.solver"
version = "0.1"

dependencies {
    implementation(project(":engine"))
    implementation(project(":methods"))

    // The spring-boot plugin manages the version of many dependencies, see:
    //   https://docs.spring.io/spring-boot/docs/3.2.1/reference/html/dependency-versions.html
    // This is why versions are not provided for the dependencies below.  It does not prevent the build from being
    // reproducible

    // Version managed by spring-boot
    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    // Version managed by spring-boot
    implementation("jakarta.validation:jakarta.validation-api")
    implementation("jakarta.annotation:jakarta.annotation-api")

    // Version managed by spring-boot
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Spring Doc dependencies
    implementation("org.springdoc:springdoc-openapi-data-rest:1.6.12")
    implementation("org.springdoc:springdoc-openapi-ui:1.6.12")
    implementation("org.springdoc:springdoc-openapi-kotlin:1.6.12")
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

/*
 * OpenAPI configuration
 */

// This needs to be modified as explained in
// https://docs.gradle.org/current/userguide/upgrading_version_8.html#project_builddir
// before upgrading to gradle 9. I am not trying to change it yet, hoping that future versions of gradle
// will offer a better upgrade path.
val generatedRoot: String = File(buildDir, "generated-src/openapi").absolutePath

sourceSets["main"].java {
    srcDirs("$generatedRoot/src/main/kotlin")
}

tasks.openApiGenerate {
    generatorName.set("kotlin-spring")
    // currently we use our own generators to allow async request handling.
    templateDir.set("$projectDir/src/main/resources/openapi-generator-7.2.0/kotlin-spring")
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
            "useSpringBoot3" to "true",
        ),
    )
}

/*
 * Configuration to generate a container image
 */

tasks.named<BootBuildImage>("bootBuildImage") {
    imageName = "registry.git.geogebra.org/solver-team/solver-engine/${project.name}:${project.version}"
    publish = true
    docker {
        publishRegistry {
            username = System.getenv("CI_REGISTRY_USER")
            password = System.getenv("CI_REGISTRY_PASSWORD")
            url = "https://registry.git.geogebra.org/v2/"
        }
    }
}

/*
 * Detekt configuration
 */

val detektKotlinVersion: String by project

// See https://github.com/detekt/detekt/issues/6198#issuecomment-1700332653 for why we need this
configurations.detekt {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion(detektKotlinVersion) // Add the version of Kotlin that detekt needs
        }
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("$rootDir/config/detekt.yaml"))
}

/*
 * Ktlint configuration
 */

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
