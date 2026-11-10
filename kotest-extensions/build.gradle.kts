plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(21)
    // Every public declaration states its visibility and type, so nothing becomes API by default.
    explicitApi()
}

dependencies {
    api(project(":core"))
    // Kotest types are part of this module's API. The JVM artifact is named directly so the Maven POM
    // points at the jar-bearing kotest-property-jvm module rather than the Kotlin Multiplatform root
    // publication.
    api(libs.kotest.property.jvm)

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
