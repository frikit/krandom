plugins {
    java
}

val krandomVersion = providers.gradleProperty("krandomVersion")
    .orElse(providers.environmentVariable("KRANDOM_VERSION"))
    .orElse("1.1.0-SNAPSHOT")

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("io.github.frikit:krandom-core:${krandomVersion.get()}")
    implementation("io.github.frikit:krandom-jackson:${krandomVersion.get()}")
    implementation("io.github.frikit:krandom-spring-boot-starter:${krandomVersion.get()}")

    testImplementation("org.springframework.boot:spring-boot-starter-test:4.0.6")
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
