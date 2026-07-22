# Java JPMS consumer

This Gradle composite verifies Java 21 named-module behavior against the locally published
`krandom-core` artifact.

- `open-consumer` proves safe constructor and mutable-field access with a qualified `opens` clause.
- `closed-consumer` proves that a missing `opens` clause fails with the exact actionable directive.

Run it through `../../scripts/verify_examples_local.sh`, or after publishing the requested kRandom
version to Maven local:

```bash
../../gradlew -PkrandomVersion=2.1.0-SNAPSHOT verifyJpms
```

## Integration consumers

`jackson-consumer` and `junit-consumer` are named modules that require the
`io.github.frikit.krandom.jackson` and `io.github.frikit.krandom.junit` automatic modules.
Because automatic modules declare no dependencies, each consumer states the transitive
requirements (`io.github.frikit.krandom`, `org.junit.jupiter.api`) explicitly. The Kotlin-based
modules (`krandom-kotest-extensions`, `krandom-kotlin-dsl`) and the Spring Boot starter are
classpath-first; their automatic module names are verified by
`scripts/verify_module_boundaries.sh`.
