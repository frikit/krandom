plugins {
    application
}

val krandomVersion = providers.gradleProperty("krandomVersion")
    .orElse(providers.environmentVariable("KRANDOM_VERSION"))
    .orElse("2.1.0-SNAPSHOT")

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("io.github.frikit:krandom-junit:${krandomVersion.get()}")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    modularity.inferModulePath.set(true)
}

application {
    mainModule.set("io.github.frikit.krandom.examples.jpms.junit")
    mainClass.set("io.github.frikit.krandom.examples.jpms.junitconsumer.JunitConsumer")
}
