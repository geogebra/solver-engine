import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")

    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":engine"))
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"

    dependsOn("processCategories")
}

val generatedRoot = "$buildDir/generated-src"
val translationsPath = "$rootDir/api/build/resources/main/static"
val translationsFileName = "ExplanationKeyToDefaultTranslation.json"


sourceSets["main"].java {
    srcDirs("$generatedRoot/main/kotlin")
}

detekt {
    buildUponDefaultConfig = true
    config = files("$rootDir/config/detekt.yaml")
}

tasks.register<processor.ProcessCategoriesTask>("processCategories") {
    categoriesRoot.set(file("$projectDir/src"))
    outputRoot.set(file(generatedRoot))
}

tasks.register<processor.ProcessTranslationsTask>("processTranslations") {
    categoriesRoot.set(file("$projectDir/src"))
    outputRoot.set(file(translationsPath))
}

ktlint {
    filter {
        exclude {
            it.file.path.contains("$buildDir")
        }
    }
}
