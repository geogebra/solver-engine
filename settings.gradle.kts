rootProject.name = "solver-engine"

include("engine")
include("methods")
include("api")
include("export")
include("methodsProcessor")

val kotlinVersion: String by settings
val kspVersion: String by settings
val kotlinBenchmarkVersion: String by settings

pluginManagement {
    plugins {
        // For some reason the global variables are not accessible here
        val kotlinVersion: String by settings
        val kspVersion: String by settings
        val kotlinBenchmarkVersion: String by settings
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.jpa") version kotlinVersion
        kotlin("plugin.spring") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        kotlin("plugin.allopen") version kotlinVersion
        id("com.google.devtools.ksp") version kspVersion

        id("me.champeau.jmh") version "0.7.1"
        id("org.jetbrains.kotlinx.benchmark") version kotlinBenchmarkVersion
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            library("ksp-api", "com.google.devtools.ksp:symbol-processing-api:$kspVersion")
        }
    }
}
