# Java Maven Integration Modules Example

This example verifies that published integration artifacts are consumable from a clean Maven build:

- `io.github.frikit:krandom-jackson` — on Jackson 3 (`tools.jackson`), `KrandomJackson.newJsonMapper()`
  writes a `Schema` as JSON Schema, and a mapper built with `KrandomJackson.configure(JsonMapper.builder())`
  writes a generated row, whose `java.time` order date is ISO-8601 text
- `io.github.frikit:krandom-spring-boot-starter` — the auto-configured `GeneratorConfig`, `ProviderHub`
  and `KrandomObjectFakerFactory`, and `krandom.*` properties (seed, locale, clock and its zone, object
  depth, collection sizes, and a safety policy) bound into the `GeneratorConfig`
- `io.github.frikit:krandom-junit` — `JunitReplayExampleTest` runs unpinned failing tests, copies the
  replay values from the printed Maven command, and replays each run to the same generated data:
  - a test: `mvn test -Dtest=... -Dkrandom.junit.recipe=base64:...`;
  - a `@ParameterizedTest` whose `@MethodSource` factory generates its arguments from the class-scoped
    configuration, which the command restores too:
    `mvn test -Dtest=... -Dkrandom.junit.recipe=base64:... -Dkrandom.junit.class-recipe='...=base64:...'`

It is executed by `../../scripts/verify_examples_local.sh` after the repository modules are published to Maven local.

```bash
mvn -Dkrandom.version=3.0.3-SNAPSHOT test
```
