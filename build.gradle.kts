plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.spotless) apply false
}

val apiBaselineVersion = providers.gradleProperty("apiBaselineVersion")
val apiCompatibilityExcludes = layout.projectDirectory.file("config/api-compatibility-excludes.txt")
val japicmpClasspath = configurations.create("japicmpClasspath") {
    isCanBeConsumed = false
    isCanBeResolved = true
    isTransitive = false
}

dependencies {
    add(
        japicmpClasspath.name,
        "com.github.siom79.japicmp:japicmp:${libs.versions.japicmp.get()}:jar-with-dependencies"
    )
}

allprojects {
    group = "io.github.frikit"
    version = (findProperty("releaseVersion") as String?) ?: "1.6.0-SNAPSHOT"

    repositories {
        if (hasProperty("useLocalMaven")) {
            mavenLocal()
        }
        mavenCentral()
    }
}

apply(plugin = "com.diffplug.spotless")

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    format("markdown") {
        target("README.md", "docs/**/*.md", "docs-site/**/*.md", "examples/**/*.md")
        // Remove trailing whitespace
        trimTrailingWhitespace()
        // Ensure files end with newline
        endWithNewline()
        // Normalize line endings
        lineEndings = com.diffplug.spotless.LineEnding.UNIX
    }
}

// Modules published to Maven Central. Their POM, sources/javadoc jars, manifest,
// and signing setup live in buildSrc/src/main/kotlin/krandom-publishing-conventions.gradle.kts.
val publishedModules = setOf("bom", "core", "jackson", "junit", "spring-boot-starter", "kotest-extensions", "kotlin-dsl")
val apiModules = publishedModules - "bom"

val apiCompatibilityTasks = apiModules.map { moduleName ->
    val taskSuffix = moduleName
        .split('-')
        .joinToString("") { part -> part.replaceFirstChar(Char::uppercaseChar) }
    val baselineConfiguration = configurations.create("${moduleName.replace("-", "")}ApiBaseline") {
        isCanBeConsumed = false
        isCanBeResolved = true
        isTransitive = false
    }
    dependencies.add(
        baselineConfiguration.name,
        "io.github.frikit:krandom-$moduleName:${apiBaselineVersion.get()}"
    )

    tasks.register<JavaExec>("check${taskSuffix}ApiCompatibility") {
        group = "verification"
        description = "Checks krandom-$moduleName against the ${apiBaselineVersion.get()} public API."
        dependsOn(":$moduleName:jar")
        classpath = japicmpClasspath
        mainClass.set("japicmp.JApiCmp")

        doFirst {
            val moduleProject = project(":$moduleName")
            val oldArtifacts = baselineConfiguration.resolvedConfiguration.resolvedArtifacts
            val oldJar = oldArtifacts.single { artifact ->
                artifact.moduleVersion.id.group == "io.github.frikit" &&
                    artifact.name == "krandom-$moduleName"
            }.file
            val newJar = moduleProject.tasks.named<org.gradle.jvm.tasks.Jar>("jar")
                .get()
                .archiveFile
                .get()
                .asFile
            val reportDirectory = layout.buildDirectory.dir("reports/japicmp/$moduleName").get().asFile
            reportDirectory.mkdirs()

            val compatibilityArgs = mutableListOf(
                "--old", oldJar.absolutePath,
                "--new", newJar.absolutePath,
                "-a", "public",
                "--only-modified",
                "--ignore-missing-classes",
                "--error-on-binary-incompatibility",
                "--error-on-source-incompatibility",
                "--html-file", reportDirectory.resolve("report.html").absolutePath,
                "--xml-file", reportDirectory.resolve("report.xml").absolutePath,
                "--report-only-filename"
            )
            val exclusions = apiCompatibilityExcludes.asFile
                .readLines()
                .map(String::trim)
                .filter { line -> line.isNotEmpty() && !line.startsWith('#') }
            if (exclusions.isNotEmpty()) {
                compatibilityArgs += listOf(
                    "--exclude", exclusions.joinToString(";"),
                    "--no-error-on-exclusion-incompatibility"
                )
            }
            args = compatibilityArgs
        }
    }
}

tasks.register("checkApiCompatibility") {
    group = "verification"
    description = "Checks all published jar modules against the configured GA baseline."
    dependsOn(apiCompatibilityTasks)
}

subprojects {
    apply(plugin = "com.diffplug.spotless")
    if (name in publishedModules) {
        apply(plugin = "krandom-publishing-conventions")
    }

    afterEvaluate {
        configure<com.diffplug.gradle.spotless.SpotlessExtension> {
            java {
                target("src/**/*.java")
                licenseHeaderFile(rootProject.file("LICENSE_HEADER"))
            }
            if (plugins.hasPlugin("org.jetbrains.kotlin.jvm")) {
                kotlin {
                    licenseHeaderFile(rootProject.file("LICENSE_HEADER"))
                }
            }
        }
    }
}
