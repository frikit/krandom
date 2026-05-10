# Java Maven Integration Modules Example

This example verifies that published integration artifacts are consumable from a clean Maven build:

- `io.github.frikit:krandom-jackson`
- `io.github.frikit:krandom-spring-boot-starter`
- `io.github.frikit:krandom-jqwik-extensions`

It is executed by `../../scripts/verify_examples_local.sh` after the repository modules are published to Maven local.

```bash
mvn -Dkrandom.version=1.1.0-SNAPSHOT test
```
