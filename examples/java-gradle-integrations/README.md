# Java Gradle Integration Modules Example

This example verifies that the locally published integration artifacts are consumable from a clean Gradle build:

- `io.github.frikit:krandom-jackson`
- `io.github.frikit:krandom-spring-boot-starter`
- `io.github.frikit:krandom-junit` — `JunitReplayExampleTest` runs an unpinned failing test, copies
  the recipe from the printed `KRANDOM_JUNIT_RECIPE=base64:... ./gradlew test --tests ... --rerun`
  command, and replays it to the same generated data

It is executed by `../../scripts/verify_examples_local.sh` after the repository modules are published to Maven local.

```bash
../../gradlew -p . -PkrandomVersion=2.6.0-SNAPSHOT test
```
