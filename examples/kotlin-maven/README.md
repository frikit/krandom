# Kotlin + Maven Example (Test-based)

Uses `io.github.frikit:krandom-core:3.0.3-SNAPSHOT` by default for repo-local verification.

Use the embedded snapshot default only for repo-local Maven-local checks. For published versions, pass the target version explicitly.

## What it shows

This example uses `krandom-core` alone, without the Kotlin DSL.

- `KotlinMavenUsageTest`: the `Generators` factories from Kotlin, and the failure that names
  `krandom-kotlin-dsl` when core alone is asked to build an immutable data class.
- `FixtureDiagnosticsExampleTest`: `ObjectFaker.strict()`, on a mutable Kotlin class, rejects a rule as
  unused because the `GeneratorConfig` excludes its field, with a `FixtureRuleException` whose report
  names the rule and its path, never a value.

The Kotlin DSL builds sealed classes and `@JvmInline` value classes: see `kotlin-gradle` and
`kotlin-maven-integrations`.

## Run

```bash
./gradlew :bom:publishToMavenLocal :core:publishToMavenLocal
cd examples/kotlin-maven
mvn -q test
```

To use a different artifact version:

```bash
mvn -q -Dkrandom.version=<version> test
```
