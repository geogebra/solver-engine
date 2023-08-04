import org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask
import org.jlleitschuh.gradle.ktlint.tasks.KtLintFormatTask

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")

    id("io.gitlab.arturbosch.detekt")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(project(":engine"))
    implementation(project(":export"))
    implementation(project(":methodsProcessor"))

    testImplementation(kotlin("test"))
    testImplementation(testFixtures(project(":engine")))

    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.2")
    testImplementation("org.junit.platform:junit-platform-launcher:1.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    ksp(project(":export"))
    ksp(project(":methodsProcessor"))
}

tasks.test {
    useJUnitPlatform()
    outputs.file("../solver-poker/test-results-src/test-results.ts")
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

tasks.withType<KtLintCheckTask> {
    // It looks like gradle cannot figure out that ktlint doesn't depend on kspKotlin and complains if we don't
    // declare this dependency
    mustRunAfter("kspKotlin")
}

tasks.withType<KtLintFormatTask> {
    // It looks like gradle cannot figure out that ktlint doesn't depend on kspKotlin and complains if we don't
    // declare this dependency
    mustRunAfter("kspKotlin")
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}

// This task depends on the results of the evaluation phase, as Gradle needs to complete
// checking which tasks are defined and configured before we can disable `kspTestKotlin`
// to not generate test directory from KSP
afterEvaluate {
    tasks.named("kspTestKotlin") {
        enabled = false
    }
}
