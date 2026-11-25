// The Android Gradle plugin compiles Kotlin itself (built-in Kotlin), with the Kotlin Gradle plugin it brings,
// 2.2.10 for 9.5.2: the build names no Kotlin version of its own.
plugins {
    id("com.android.library") version "9.5.2" apply false
}

// The kRandom version that the modules' unit tests use.
extra["krandomVersion"] =
    providers
        .gradleProperty("krandomVersion")
        .orElse(providers.environmentVariable("KRANDOM_VERSION"))
        .orElse("4.2.0-SNAPSHOT")
        .get()
