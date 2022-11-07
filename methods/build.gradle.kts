import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")

    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
    id("com.google.devtools.ksp") version "1.7.10-1.0.6"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":engine"))
    implementation(project(":export"))
    implementation(project(":methodsProcessor"))

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.2")

    ksp(project(":export"))
    ksp(project(":methodsProcessor"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

detekt {
    buildUponDefaultConfig = true
    config = files("$rootDir/config/detekt.yaml")
}

ktlint {
    filter {
        exclude {
            it.file.path.contains("$buildDir")
        }
    }
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}
