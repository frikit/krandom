plugins {
    java
}

val krandomVersion =
    providers
        .gradleProperty("krandomVersion")
        .orElse(providers.environmentVariable("KRANDOM_VERSION"))
        .orElse("3.0.3-SNAPSHOT")

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
        // Java 21 unless -PjavaVersion names another: scripts/verify_examples_local.sh also runs these tests
        // with -PjavaVersion=17, the oldest Java that kRandom's Java modules run on.
        languageVersion.set(
            providers
                .gradleProperty("javaVersion")
                .map { version -> JavaLanguageVersion.of(version) }
                .orElse(JavaLanguageVersion.of(21)),
        )
    }
}
