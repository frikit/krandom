plugins {
    kotlin("jvm") version "2.3.10"
}

val krandomVersion = providers.gradleProperty("krandomVersion")
    .orElse(providers.environmentVariable("KRANDOM_VERSION"))
    .orElse("1.5.0-SNAPSHOT")

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("io.github.frikit:krandom-core:${krandomVersion.get()}")
    testImplementation("io.github.frikit:krandom-kotest-extensions:${krandomVersion.get()}")
    testImplementation("io.github.frikit:krandom-kotlin-dsl:${krandomVersion.get()}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:6.0.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:6.0.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.0.3")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
