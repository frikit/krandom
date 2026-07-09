/*
 * Publishing conventions for kRandom's published modules.
 *
 * Applies maven-publish + signing, attaches sources/javadoc jars, sets the
 * Automatic-Module-Name manifest attribute, and creates the "mavenJava"
 * publication with the module's Central-Portal-ready POM. Signing activates
 * only when GPG_SIGNING_KEY and GPG_SIGNING_PASSWORD are present in the
 * environment (CI release workflow).
 *
 * Maven Central uploads are exposed to the root com.gradleup.nmcp.aggregation
 * plugin. Run
 * `./gradlew publishAggregationToCentralPortal` to push the signed bundle
 * to https://central.sonatype.com.
 */
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.tasks.Jar

plugins {
    `maven-publish`
    signing
}

apply(plugin = "com.gradleup.nmcp")

val moduleDescriptions = mapOf(
    "bom" to "kRandom bill of materials for aligning all published kRandom module versions.",
    "core" to "kRandom core: Java 21 random and fake-data generation toolkit with seedable generators, locale-aware data, ObjectGenerator/ObjectFaker, and Schema export.",
    "jackson" to "Jackson serialization integration for kRandom generators (databind module wiring on top of krandom-core).",
    "junit" to "JUnit 5 extension for kRandom — per-test seed management with @KrandomSeed pinning, seeded GeneratorConfig parameter injection, and failure-seed reporting for reproducible test runs.",
    "spring-boot-starter" to "Spring Boot 4.x auto-configuration for kRandom — registers GeneratorConfig, ProviderHub, and KrandomObjectFakerFactory beans driven by krandom.* application properties.",
    "kotest-extensions" to "Kotest Arb adapters for kRandom generators, enabling property-based testing on top of krandom-core.",
    "kotlin-dsl" to "Kotlin DSL builder for kRandom object-generation rules — fluent fixture configuration on top of krandom-core."
)
val moduleNames = mapOf(
    "core" to "io.github.frikit.krandom",
    "jackson" to "io.github.frikit.krandom.jackson",
    "junit" to "io.github.frikit.krandom.junit",
    "spring-boot-starter" to "io.github.frikit.krandom.spring.boot.starter",
    "kotest-extensions" to "io.github.frikit.krandom.kotest",
    "kotlin-dsl" to "io.github.frikit.krandom.kotlin.dsl"
)

afterEvaluate {
    val componentName = when {
        components.names.contains("java") -> "java"
        components.names.contains("kotlin") -> "kotlin"
        components.names.contains("javaPlatform") -> "javaPlatform"
        else -> null
    }
    if (componentName == null || project.name !in moduleDescriptions.keys) {
        return@afterEvaluate
    }

    if (componentName != "javaPlatform") {
        extensions.findByType(JavaPluginExtension::class.java)?.apply {
            withSourcesJar()
            withJavadocJar()
        }

        tasks.withType<Jar>().configureEach {
            from(rootProject.file("LICENSE")) {
                into("META-INF")
            }
        }

        tasks.named<Jar>("jar").configure {
            manifest {
                attributes("Automatic-Module-Name" to moduleNames.getValue(project.name))
            }
        }
    }

    publishing {
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
    }

    signing {
        val signingKey = providers.environmentVariable("GPG_SIGNING_KEY").orNull
        val signingPassword = providers.environmentVariable("GPG_SIGNING_PASSWORD").orNull
        if (signingKey != null && signingPassword != null) {
            useInMemoryPgpKeys(signingKey, signingPassword)
            sign(publishing.publications["mavenJava"])
        }
    }
}
