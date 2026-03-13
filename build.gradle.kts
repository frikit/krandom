import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.spotless) apply false
}

allprojects {
    group = "io.github.frikit"
    version = (findProperty("releaseVersion") as String?) ?: "0.1.0-SNAPSHOT"

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
    apply(plugin = "maven-publish")

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

        extensions.findByType(JavaPluginExtension::class.java)?.apply {
            withSourcesJar()
            withJavadocJar()
        }

        val componentName = when {
            components.names.contains("java") -> "java"
            components.names.contains("kotlin") -> "kotlin"
            else -> null
        }

        if (componentName != null) {
            configure<PublishingExtension> {
                publications {
                    if (findByName("mavenJava") == null) {
                        create<MavenPublication>("mavenJava") {
                            artifactId = "krandom-${project.name}"
                            from(components[componentName])
                            pom {
                                name.set("krandom-${project.name}")
                                description.set("kRandom ${project.name} module")
                                url.set("https://github.com/frikit/krandom")
                                licenses {
                                    license {
                                        name.set("MIT License")
                                        url.set("https://opensource.org/license/mit/")
                                    }
                                }
                                scm {
                                    url.set("https://github.com/frikit/krandom")
                                    connection.set("scm:git:https://github.com/frikit/krandom.git")
                                    developerConnection.set("scm:git:ssh://git@github.com/frikit/krandom.git")
                                }
                            }
                        }
                    }
                }
                repositories {
                    maven {
                        name = "GitHubPackages"
                        val repo = providers.environmentVariable("GITHUB_REPOSITORY")
                            .orElse("frikit/krandom")
                            .get()
                        url = uri("https://maven.pkg.github.com/$repo")
                        credentials {
                            username = providers.environmentVariable("GITHUB_ACTOR")
                                .orElse(providers.gradleProperty("gpr.user"))
                                .orNull
                            password = providers.environmentVariable("GITHUB_TOKEN")
                                .orElse(providers.gradleProperty("gpr.key"))
                                .orNull
                        }
                    }
                }
            }
        }
    }
}
