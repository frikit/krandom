plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    api(project(":core"))
    api(libs.spring.boot.autoconfigure)
    // The @KrandomTest slice meta-annotations come from the Spring Boot test stack; consumers
    // already provide it through spring-boot-starter-test, so it must not leak into the POM.
    compileOnly(libs.spring.boot.starter.test)
    compileOnly(libs.junit.jupiter.api)
    annotationProcessor(libs.spring.boot.configuration.processor)

    testImplementation(libs.spring.boot.starter.test)
    // Jackson 3 (tools.jackson), the generation Spring Boot 4 auto-configures by default; the tests
    // read the packaged configuration metadata with it.
    testImplementation(libs.jackson.databind)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
