# Kotlin + Android Example (Local Unit Tests)

Uses `io.github.frikit:krandom-core:3.1.0-SNAPSHOT` by default for repo-local verification.

Use the embedded snapshot default only for repo-local Maven-local checks. For published versions, pass the target version explicitly.

Two Android library modules use kRandom in their local unit tests (`src/test`), which Gradle runs on a JVM
on the build machine, not on a device. They are built with the Android Gradle plugin 9.5.1 and its
built-in Kotlin, with the Kotlin Gradle plugin 2.4.21, against `compileSdk` 36, and compile Java and
Kotlin for JVM target 17. The build sets no Java toolchain, so the tests run on the Java that runs Gradle,
which must be Java 21 or later because `krandom-core` is compiled for Java 21.

## What it shows

- `contacts`, with JUnit 4, the Android default: `ContactFixturesTest` uses `Krandom.seeded(...)` and the
  `Generators` factories, and builds the `Contact` data class with `krandom<Contact>()` from
  `krandom-kotlin-dsl`. `Contact` holds an `android.net.Uri`, an abstract class that kRandom cannot build,
  so the test gives `Uri` a rule, `ruleForType<Uri> { photo }`, that returns a Mockito mock, and checks that
  without the rule generation fails with an `ObjectGenerationException` that names `android.net.Uri`.
- `junit-platform`, which runs its unit tests on the JUnit Platform
  (`testOptions.unitTests.all { it.useJUnitPlatform() }`): `KrandomExtensionTest` uses the `krandom-junit`
  extension, which injects a seeded `Krandom` and `GeneratorConfig` into each test and pins a seed with
  `@KrandomSeed`, and seeds `krandom<Badge>()` with the pinned seed.

## Run

The build needs an Android SDK with platform 36, found through `ANDROID_HOME`.

```bash
./gradlew :bom:publishToMavenLocal :core:publishToMavenLocal :junit:publishToMavenLocal :kotlin-dsl:publishToMavenLocal
cd examples/kotlin-android
./gradlew test
```

To use a different artifact version:

```bash
./gradlew -PkrandomVersion=<version> test
```
