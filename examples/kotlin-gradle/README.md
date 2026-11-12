# Kotlin + Gradle Example (Test-based)

Uses `io.github.frikit:krandom-core:3.1.0-SNAPSHOT` by default for repo-local verification.

Use the embedded snapshot default only for repo-local Maven-local checks. For published versions, pass the target version explicitly.

## What it shows

- `KotlinGradleUsageTest`: the `Generators` factories from Kotlin, a `krandomArb` Kotest arbitrary from
  `krandom-kotest-extensions`, and the `krandom-kotlin-dsl` builders `krandom` and `krandomList`, for a
  mutable class and for an immutable data class, whose parameter default is kept.
- `SealedAndValueClassExampleTest`: the Kotlin DSL generates a sealed class as one of its subclasses, a
  `data object` among them, and a `@JvmInline` value class, whose `init` block checks the email address
  generated for it from the parameter's name.
- `GenerationReportExampleTest`: `config { generationReportListener { ... } }` in the DSL receives an
  entry for each generated property, and the test asserts on an entry's path, step, generator and
  matched rule, never on a value.

## Run

```bash
./gradlew :bom:publishToMavenLocal :core:publishToMavenLocal :kotest-extensions:publishToMavenLocal :kotlin-dsl:publishToMavenLocal
cd examples/kotlin-gradle
./gradlew test
```

To use a different artifact version:

```bash
./gradlew -PkrandomVersion=<version> test
```
