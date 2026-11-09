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
    implementation(kotlin("reflect"))

    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.jakarta.validation.api)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform {
        includeEngines("kotest", "junit-jupiter")
    }
}
