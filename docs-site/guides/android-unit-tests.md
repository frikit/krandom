---
layout: page
title: Android Unit Tests
permalink: /guides/android-unit-tests/
---

# Android Unit Tests

An Android module can use kRandom in its local unit tests, the tests under `src/test` that Gradle runs on
a JVM on the build machine. The
[`examples/kotlin-android`](https://github.com/frikit/krandom/tree/main/examples/kotlin-android) example,
which CI runs, has two Android library modules built with the Android Gradle plugin 9.5.1 and its built-in
Kotlin. They compile Java and Kotlin for JVM target 17 against `compileSdk` 36, and their unit tests use
`krandom-core`, `krandom-kotlin-dsl` and `krandom-junit`.

## Run the tests on Java 21

`krandom-core` is compiled for Java 21, so the unit tests need Java 21 or later. The example sets no Java
toolchain, so Gradle runs its unit tests on the Java that runs Gradle, the Gradle JDK, which must therefore
be Java 21 or later. CI runs the example with Gradle on Java 21.

## Compile for JVM target 17

From 3.1.0, `krandom-kotlin-dsl` and `krandom-kotest-extensions` are compiled for JVM target 17, so Kotlin
code compiled for JVM target 17, as Android modules usually are, can call their inline functions, such as
`krandom<T>()`. Up to 3.0.x they are compiled for JVM target 21, and the Kotlin compiler stops with "Cannot
inline bytecode built with JVM target 21 into bytecode that is being built with JVM target 17".
`krandom-core` is still compiled for Java 21: the example's Kotlin code, compiled for JVM target 17, calls it,
and its unit tests run it on Java 21.

The build file of the example's `contacts` module, whose tests use JUnit 4, the Android default:

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
}

android {
    namespace = "io.github.frikit.krandom.examples.android.contacts"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    // 3.1.0 or later: earlier versions compile krandom-kotlin-dsl for JVM target 21.
    testImplementation(platform("io.github.frikit:krandom-bom:3.1.0"))
    testImplementation("io.github.frikit:krandom-core")
    testImplementation("io.github.frikit:krandom-kotlin-dsl")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.24.0")
}
```

The Android Gradle plugin compiles Kotlin with the Kotlin Gradle plugin on the build classpath. The Android
Gradle plugin 9.5.1 brings version 2.2.10, whose compiler cannot read the Kotlin 2.4 metadata of
`krandom-kotlin-dsl`: it stops with "Module was compiled with an incompatible version of Kotlin. The binary
version of its metadata is 2.4.0, expected version is 2.2.0". The example puts the Kotlin Gradle plugin
2.4.21, the version that builds kRandom, on the classpath in the build file of its root project:

```kotlin
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
```

## Give Android types a rule

In a local unit test, the Android framework classes come from the SDK's `android.jar`, not from a device.
kRandom cannot build `android.net.Uri`, an abstract class, so `krandom<Contact>()` for a `Contact` with a
`Uri` property fails with an `ObjectGenerationException` whose message names
`declared type android.net.Uri`. Give such a type a rule that returns the value the test needs. The example
returns a Mockito mock:

```kotlin
data class Contact(
    val id: Long,
    val name: String,
    val email: String,
    val photo: Uri,
)

@Test
fun `the Kotlin DSL builds a data class whose Uri comes from a rule`() {
    val photo = mock(Uri::class.java)

    val contact =
        krandom<Contact> {
            config { seed(42L) }
            ruleForType<Uri> { photo }
        }

    assertSame(photo, contact.photo)
}
```

## krandom-junit on the JUnit Platform

`krandom-junit` is a JUnit Jupiter extension, so tests that use it run on the JUnit Platform. The example's
`junit-platform` module runs its unit tests there, with the Jupiter engine and the platform launcher on the
test runtime classpath:

```kotlin
android {
    testOptions {
        unitTests.all { test ->
            test.useJUnitPlatform()
        }
    }
}

dependencies {
    testImplementation(platform("io.github.frikit:krandom-bom:3.1.0"))
    testImplementation("io.github.frikit:krandom-junit")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:6.1.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.1.3")
}
```

`unitTests.all { }` configures every unit-test task of the module, so the example keeps its JUnit 4 tests in
the `contacts` module. The [JUnit extension guide]({{ '/guides/junit-extension/' | relative_url }})
describes the injected parameters and `@KrandomSeed`.

## Not supported: device tests and app code

kRandom is not supported in on-device (instrumented) tests under `src/androidTest` or in app code. It is
built and tested only on JVMs, on Java 21 and 25 in CI, and `krandom-core` calls Java 17 and Java 21 library
APIs, such as `java.util.random.RandomGenerator`, `List.getFirst()` and `Math.clamp`, that the Android API
includes only from level 35. Add kRandom with `testImplementation`, not with `implementation` or
`androidTestImplementation`.
