import org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask

plugins {
    kotlin("jvm")

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

    ksp(project(":export"))
    ksp(project(":methodsProcessor"))
}

tasks.test {
    useJUnitPlatform()
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
