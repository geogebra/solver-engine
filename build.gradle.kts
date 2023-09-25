import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    kotlin("jvm")

    id("org.jlleitschuh.gradle.ktlint") version "11.5.1" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.1" apply false
}

group = "org.geogebra.solver"
version = "0.1"

subprojects {

    // It would be better to handle this in a "conventions plugin" (see
    // https://docs.gradle.org/current/samples/sample_convention_plugins.html). However I have not been able to make
    // that work.
    apply(plugin = "kotlin")
    configure<KotlinJvmProjectExtension> {
        jvmToolchain(17)
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions.allWarningsAsErrors = false
    }

    // The IntelliJ ktlint plugin v0.13 supports ktlint 0.48.2 so we stick with that version for now in order to get
    // consistent results from IDE and gradle.
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    configure<KtlintExtension> {
        version.set("0.48.2")
    }

    // Detekt only supports jvm up to 17 according to its documentation (in practice it seem 18 works too)
    tasks.withType<Detekt>().configureEach {
        jvmTarget = "17"
    }
    tasks.withType<DetektCreateBaselineTask>().configureEach {
        jvmTarget = "17"
    }
}
