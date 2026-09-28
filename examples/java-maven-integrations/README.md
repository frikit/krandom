# Java Maven Integration Modules Example

This example verifies that published integration artifacts are consumable from a clean Maven build:

- `io.github.frikit:krandom-jackson`
- `io.github.frikit:krandom-spring-boot-starter`
- `io.github.frikit:krandom-junit` — `JunitReplayExampleTest` runs an unpinned failing test, copies
  the recipe from the printed `mvn test -Dtest=... -Dkrandom.junit.recipe=base64:...` command, and
  replays it to the same generated data

It is executed by `../../scripts/verify_examples_local.sh` after the repository modules are published to Maven local.

```bash
mvn -Dkrandom.version=2.6.0-SNAPSHOT test
```
