import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    kotlin("jvm")

    id("org.jlleitschuh.gradle.ktlint") version "11.5.0" apply false

    // 1.23 is available BUT upgrading is causing some error whose solution is unclear to me.
    // - 1.23.0 wants kotlin 1.8.21 and we have 1.8.22
    // - 1.23.1 wants kotlin 1.9.0 and we have 1.8.22
    // See https://detekt.dev/docs/gettingstarted/gradle/#dependencies
    id("io.gitlab.arturbosch.detekt") version "1.22.0" apply false
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
