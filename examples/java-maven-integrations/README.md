# Java Maven Integration Modules Example

This example verifies that published integration artifacts are consumable from a clean Maven build:

- `io.github.frikit:krandom-jackson`
- `io.github.frikit:krandom-spring-boot-starter`

It is executed by `../../scripts/verify_examples_local.sh` after the repository modules are published to Maven local.

```bash
mvn -Dkrandom.version=2.5.0-SNAPSHOT test
```
