# kRandom Examples Monorepo

This folder contains test-based examples for each supported language/build-tool combination.

Core examples verify direct `io.github.frikit:krandom-core` consumption. Integration examples verify the published Jackson, Spring Boot, Kotest, and Kotlin DSL artifacts from clean consumer builds.

Default example version: `1.1.0-SNAPSHOT`

Version strategy:

- `1.1.0-SNAPSHOT` is the repo-local default for Maven-local verification.
- CI injects the version under test instead of editing example build files.
- published-release checks should pass an explicit version with the existing property knobs rather than changing the embedded defaults.

For repo-local verification, publish the current workspace artifact to Maven local first:

```bash
./scripts/verify_examples_local.sh
```

That script:

- verifies Java 21+
- publishes all current `krandom-*` consumer artifacts to Maven local
- runs the example test suites against the local artifact

To point the examples at a different artifact version, set `KRANDOM_VERSION` or the build-tool-specific property:

- Gradle: `-PkrandomVersion=<version>`
- Maven: `-Dkrandom.version=<version>`
- sbt / Mill: `-Dkrandom.version=<version>`

## Matrix

- Java + Gradle: `examples/java-gradle`
- Java + Gradle integration modules: `examples/java-gradle-integrations`
- Java + Maven: `examples/java-maven`
- Java + Maven integration modules: `examples/java-maven-integrations`
- Kotlin + Gradle: `examples/kotlin-gradle`
- Kotlin + Maven: `examples/kotlin-maven`
- Kotlin + Maven integration modules: `examples/kotlin-maven-integrations`
- Scala + sbt: `examples/scala-sbt`
- Scala + Mill: `examples/scala-mill`

## Run commands

```bash
# Verify the whole matrix against the locally published snapshot
./scripts/verify_examples_local.sh
```
