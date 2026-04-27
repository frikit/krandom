# Kotlin Maven Integration Modules Example

This example verifies that published Kotlin-facing integration artifacts are consumable from a clean Maven build:

- `io.github.frikit:krandom-kotest-extensions`
- `io.github.frikit:krandom-kotlin-dsl`

It is executed by `../../scripts/verify_examples_local.sh` after the repository modules are published to Maven local.

```bash
mvn -Dkrandom.version=0.1.0-SNAPSHOT test
```
