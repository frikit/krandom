import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(21)
    // Every public declaration states its visibility and type, so nothing becomes API by default.
    explicitApi()
    // Bytecode for Java 17, against the Java 17 API, so that code compiled for JVM target 17, as Android modules
    // usually are, can inline this module's inline functions. krandom-core still needs a Java 21 runtime.
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.add("-Xjdk-release=17")
    }
}

java {
    // krandom-core is a Java 21 library, so this module's own classpaths must not ask for Java 17 variants.
    disableAutoTargetJvm()
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
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
