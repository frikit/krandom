// The Android Gradle plugin compiles Kotlin itself (built-in Kotlin) with the Kotlin Gradle plugin on the build
// classpath, which is its own dependency, 2.2.10, unless the build names a later one. This example uses the Kotlin
// version that builds kRandom.
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.21")
    }
}

plugins {
    id("com.android.library") version "9.5.1" apply false
}

// The kRandom version that the modules' unit tests use.
extra["krandomVersion"] =
    providers
        .gradleProperty("krandomVersion")
        .orElse(providers.environmentVariable("KRANDOM_VERSION"))
        .orElse("3.1.0-SNAPSHOT")
        .get()
