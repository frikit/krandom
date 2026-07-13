plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    api(project(":core"))
    api(libs.kotest.property)

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
// module is compiled and tested against (e.g. ./gradlew :kotest-extensions:test -PkotestVersion=6.1.11).
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
