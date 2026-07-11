plugins {
    kotlin("jvm") version "2.4.0"
}

val krandomVersion = providers.gradleProperty("krandomVersion")
    .orElse(providers.environmentVariable("KRANDOM_VERSION"))
    .orElse("2.0.0-SNAPSHOT")

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(platform("io.github.frikit:krandom-bom:${krandomVersion.get()}"))
    implementation("io.github.frikit:krandom-core")
    testImplementation("io.github.frikit:krandom-kotest-extensions")
    testImplementation("io.github.frikit:krandom-kotlin-dsl")
    testImplementation("org.junit.jupiter:junit-jupiter-api:6.1.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:6.1.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.1.1")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
