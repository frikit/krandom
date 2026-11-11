# kRandom Examples Monorepo

This folder contains test-based examples for each supported language/build-tool combination.

Core examples verify direct `io.github.frikit:krandom-core` consumption, including strong
encapsulation from Java 21 named modules. Integration examples verify the published Jackson, JUnit,
Spring Boot, Kotest, and Kotlin DSL artifacts from clean consumer builds.

Default example version: `3.0.1-SNAPSHOT`

Version strategy:

- `3.0.1-SNAPSHOT` is the repo-local default for Maven-local verification.
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
- verifies both successful qualified `opens` and actionable missing-`opens` behavior from named modules

To point the examples at a different artifact version, set `KRANDOM_VERSION` or the build-tool-specific property:

- Gradle: `-PkrandomVersion=<version>`
- Maven: `-Dkrandom.version=<version>`
- sbt / Mill: `-Dkrandom.version=<version>`

## Matrix

Each example's README lists what its tests show.

- Java + Gradle: `examples/java-gradle` — the `Generators` factories, `ObjectModel`, scoped locale data,
  `ObjectFaker.strict()` fixture diagnostics, the generation report, and sealed interfaces
- Java + Gradle integration modules: `examples/java-gradle-integrations` — `krandom-jackson` on
  Jackson 3, the Spring Boot starter and its `krandom.*` properties, and `krandom-junit` replay of a
  failing test and of a `@MethodSource` factory's arguments
- Java + Maven: `examples/java-maven` — the `Generators` factories, `ObjectModel`,
  `ObjectFaker.strict()` fixture diagnostics, the generation report, and sealed interfaces
- Java + Maven integration modules: `examples/java-maven-integrations` — the Gradle integration
  example's checks, with the Maven replay commands
- Java 21 + Gradle named modules: `examples/java-jpms` — qualified `opens` for object generation, the
  failure without it, and named-module Jackson and JUnit consumers
- Kotlin + Gradle: `examples/kotlin-gradle` — the `Generators` factories, a Kotest arbitrary, and the
  Kotlin DSL with sealed classes, value classes, and the generation report
- Kotlin + Maven: `examples/kotlin-maven` — `krandom-core` alone from Kotlin, with
  `ObjectFaker.strict()` fixture diagnostics on a mutable class
- Kotlin + Maven integration modules: `examples/kotlin-maven-integrations` — a Kotest arbitrary, the
  Kotlin DSL with sealed classes and value classes, the Spring Boot starter and its `krandom.*`
  properties, and `krandom-jackson` on Jackson 3
- Scala + sbt: `examples/scala-sbt` — the `Generators` factories from Scala 3
- Scala + Mill: `examples/scala-mill` — the `Generators` factories from Scala 3

## Run commands

```bash
# Verify the whole matrix against the locally published snapshot
./scripts/verify_examples_local.sh
```

## Notes for Scala callers

kRandom's `Generator<T>` has a single abstract method, so Scala 3 converts a lambda such as
`() => source.generate()` to a `Generator`. If a Scala helper is overloaded on both
`Generator[String]` and `() => String`, a lambda argument can resolve to the `Generator` overload,
and an overload that delegates to the other with a lambda then calls itself forever. Give such
helpers distinct names.

`Krandom.pick` takes Java varargs, so `fake.pick(Seq("a", "b"))` passes the whole `Seq` as one
value and returns it. Write `fake.pick("a", "b")`, or splice a collection with `fake.pick(values*)`
(Scala 2.13: `fake.pick(values: _*)`).
