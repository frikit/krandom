plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    api(project(":core"))
    // Kotest types are part of this module's API, but the consumer owns the Kotest version: a soft
    // "prefer" constraint lets a consumer's own kotest-property declaration (6.1+) win instead of
    // being upgraded by Gradle conflict resolution to the version this module was built with.
    // The JVM artifact is named directly so the Maven POM keeps pointing at the jar-bearing
    // kotest-property-jvm module rather than the Kotlin Multiplatform root publication.
    api("io.kotest:kotest-property-jvm") {
        version {
            prefer(libs.versions.kotest.get())
        }
    }

    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform {
        includeEngines("kotest", "junit-jupiter")
    }
}

// Supported-version-range verification: -PkotestVersion=<version> forces the Kotest line the
// module is compiled and tested against (e.g. ./gradlew :kotest-extensions:test -PkotestVersion=6.1.11
// --dependency-verification=lenient; the verification metadata lists only the default Kotest version).
val kotestVersionOverride = providers.gradleProperty("kotestVersion")
if (kotestVersionOverride.isPresent) {
    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "io.kotest") {
                useVersion(kotestVersionOverride.get())
                because("kotest version-range verification")
            }
        }
    }
}
