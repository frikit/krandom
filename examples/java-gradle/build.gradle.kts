plugins {
    java
}

val krandomVersion = providers.gradleProperty("krandomVersion")
    .orElse(providers.environmentVariable("KRANDOM_VERSION"))
    .orElse("0.1.0-SNAPSHOT")

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("io.github.frikit:krandom-core:${krandomVersion.get()}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:6.0.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:6.0.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.0.3")
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
