plugins {
    java
}

val krandomVersion = providers.gradleProperty("krandomVersion")
    .orElse(providers.environmentVariable("KRANDOM_VERSION"))
    .orElse("1.6.0-SNAPSHOT")

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(platform("io.github.frikit:krandom-bom:${krandomVersion.get()}"))
    implementation("io.github.frikit:krandom-core")
    implementation("io.github.frikit:krandom-jackson")
    implementation("io.github.frikit:krandom-spring-boot-starter")

    testImplementation("org.springframework.boot:spring-boot-starter-test:4.1.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:6.1.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:6.1.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.1.1")
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
