import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.plugins.signing.SigningExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.spotless) apply false
    signing
}

allprojects {
    group = "io.github.frikit"
    version = (findProperty("releaseVersion") as String?) ?: "1.1.0-SNAPSHOT"

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

subprojects {
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

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

        val moduleDescriptions = mapOf(
            "core" to "kRandom core: Java 21 random and fake-data generation toolkit with seedable generators, locale-aware data, ObjectGenerator/ObjectFaker, and Schema export.",
            "jackson" to "Jackson serialization integration for kRandom generators (databind module wiring on top of krandom-core).",
            "spring-boot-starter" to "Spring Boot 4.x auto-configuration for kRandom — registers GeneratorConfig, ProviderHub, and KrandomObjectFakerFactory beans driven by krandom.* application properties.",
            "kotest-extensions" to "Kotest Arb adapters for kRandom generators, enabling property-based testing on top of krandom-core.",
            "kotlin-dsl" to "Kotlin DSL builder for kRandom object-generation rules — fluent fixture configuration on top of krandom-core."
        )
        val moduleNames = mapOf(
            "core" to "io.github.frikit.krandom",
            "jackson" to "io.github.frikit.krandom.jackson",
            "spring-boot-starter" to "io.github.frikit.krandom.spring.boot.starter",
            "kotest-extensions" to "io.github.frikit.krandom.kotest",
            "kotlin-dsl" to "io.github.frikit.krandom.kotlin.dsl"
        )

        if (componentName != null && project.name in moduleDescriptions.keys) {
            tasks.named<Jar>("jar").configure {
                manifest {
                    attributes("Automatic-Module-Name" to moduleNames.getValue(project.name))
                }
            }

            configure<PublishingExtension> {
                publications {
                    if (findByName("mavenJava") == null) {
                        create<MavenPublication>("mavenJava") {
                            val publishedArtifactId = "krandom-${project.name}"
                            artifactId = publishedArtifactId
                            from(components[componentName])
                            pom {
                                name.set(publishedArtifactId)
                                description.set(moduleDescriptions.getValue(project.name))
                                url.set("https://github.com/frikit/krandom")
                                licenses {
                                    license {
                                        name.set("MIT License")
                                        url.set("https://opensource.org/license/mit/")
                                    }
                                }
                                developers {
                                    developer {
                                        id.set("frikit")
                                        name.set("Victor Osipov")
                                        email.set("ofrikit94@gmail.com")
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
                // Maven Central uploads are aggregated by the com.gradleup.nmcp.settings
                // plugin (configured in settings.gradle.kts). Run
                // `./gradlew publishAggregationToCentralPortal` to push the signed bundle
                // to https://central.sonatype.com.
            }

            configure<SigningExtension> {
                val signingKey = providers.environmentVariable("GPG_SIGNING_KEY").orNull
                val signingPassword = providers.environmentVariable("GPG_SIGNING_PASSWORD").orNull
                if (signingKey != null && signingPassword != null) {
                    useInMemoryPgpKeys(signingKey, signingPassword)
                    sign(the<PublishingExtension>().publications["mavenJava"])
                }
            }
        }
    }
}
