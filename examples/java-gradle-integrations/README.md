# Java Gradle Integration Modules Example

This example verifies that the locally published integration artifacts are consumable from a clean Gradle build:

- `io.github.frikit:krandom-jackson`
- `io.github.frikit:krandom-spring-boot-starter`

It is executed by `../../scripts/verify_examples_local.sh` after the repository modules are published to Maven local.

```bash
../../gradlew -p . -PkrandomVersion=2.1.0-SNAPSHOT test
```
