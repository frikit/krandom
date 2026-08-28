plugins {
    java
}

val krandomVersion = providers.gradleProperty("krandomVersion")
    .orElse(providers.environmentVariable("KRANDOM_VERSION"))
    .orElse("3.0.0-SNAPSHOT")

repositories {
    if (providers.gradleProperty("krandomRepository").orNull != "central") {
        mavenLocal()
    }
    mavenCentral()
}

dependencies {
    implementation(platform("io.github.frikit:krandom-bom:${krandomVersion.get()}"))
    implementation("io.github.frikit:krandom-core")
    testImplementation("org.junit.jupiter:junit-jupiter-api:6.1.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:6.1.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.1.3")
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
