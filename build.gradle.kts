plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.spotless) apply false
}

allprojects {
    group = "org.github.krandom"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenLocal()
        mavenCentral()
    }
}

apply(plugin = "com.diffplug.spotless")

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    format("markdown") {
        target("README.md", "docs/**/*.md")
        // Remove trailing whitespace
        trimTrailingWhitespace()
        // Ensure files end with newline
        endWithNewline()
        // Normalize line endings
        lineEndings = com.diffplug.spotless.LineEnding.UNIX
    }
}

subprojects {
    apply(plugin = "com.diffplug.spotless")

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
