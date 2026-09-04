plugins {
    application
}

val krandomVersion = providers.gradleProperty("krandomVersion")
    .orElse(providers.environmentVariable("KRANDOM_VERSION"))
    .orElse("2.4.0-SNAPSHOT")

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("io.github.frikit:krandom-core:${krandomVersion.get()}")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    modularity.inferModulePath.set(true)
}

application {
    mainModule.set("io.github.frikit.krandom.examples.jpms.closed")
    mainClass.set("io.github.frikit.krandom.examples.jpms.closedconsumer.ClosedConsumer")
}
