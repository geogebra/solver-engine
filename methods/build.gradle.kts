

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

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}
