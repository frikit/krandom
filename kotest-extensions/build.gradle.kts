import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(21)
    // Every public declaration states its visibility and type, so nothing becomes API by default.
    explicitApi()
    // Bytecode for Java 17, against the Java 17 API, so that code compiled for JVM target 17, as Android modules
    // usually are, can inline this module's inline functions.
    // Kotlin 2.2 metadata and standard library API, so that the Kotlin 2.2 compiler, which the Android Gradle
    // plugin 9.5.1 brings, can read this module; the published kotlin-stdlib dependency is 2.2.21 for the same
    // reason, since the compiler reads the standard library on the classpath too.
    coreLibrariesVersion = "2.2.21"
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.add("-Xjdk-release=17")
        languageVersion.set(KotlinVersion.KOTLIN_2_2)
        apiVersion.set(KotlinVersion.KOTLIN_2_2)
    }
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
