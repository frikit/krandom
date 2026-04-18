# kRandom Examples Monorepo

This folder contains test-based examples for each supported language/build-tool combination.

All examples depend directly on `io.github.frikit:krandom-core`.

Default example version: `0.1.0-SNAPSHOT`

For repo-local verification, publish the current workspace artifact to Maven local first:

```bash
./scripts/verify_examples_local.sh
```

That script:

- verifies Java 21+
- publishes `krandom-core` to Maven local
- runs the example test suites against the local artifact

To point the examples at a different artifact version, set `KRANDOM_VERSION` or the build-tool-specific property:

- Gradle: `-PkrandomVersion=<version>`
- Maven: `-Dkrandom.version=<version>`
- sbt / Mill: `-Dkrandom.version=<version>`

## Matrix

- Java + Gradle: `examples/java-gradle`
- Java + Maven: `examples/java-maven`
- Kotlin + Gradle: `examples/kotlin-gradle`
- Kotlin + Maven: `examples/kotlin-maven`
- Scala + sbt: `examples/scala-sbt`
- Scala + Mill: `examples/scala-mill`

## Run commands

```bash
# Verify the whole matrix against the locally published snapshot
./scripts/verify_examples_local.sh
```
