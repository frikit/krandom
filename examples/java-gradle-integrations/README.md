# Java Gradle Integration Modules Example

This example verifies that the locally published integration artifacts are consumable from a clean Gradle build:

- `io.github.frikit:krandom-jackson` — on Jackson 3 (`tools.jackson`), `KrandomJackson.newJsonMapper()`
  writes a `Schema` as JSON Schema, and a mapper built with `KrandomJackson.configure(JsonMapper.builder())`
  writes a generated row, whose `java.time` order date is ISO-8601 text
- `io.github.frikit:krandom-spring-boot-starter` — the auto-configured `GeneratorConfig`, `ProviderHub`
  and `KrandomObjectFakerFactory`, and `krandom.*` properties (seed, locale, clock and its zone, object
  depth, collection sizes, and a safety policy) bound into the `GeneratorConfig`
- `io.github.frikit:krandom-junit` — `JunitReplayExampleTest` runs unpinned failing tests, copies the
  replay values from the printed Gradle command, and replays each run to the same generated data:
  - a test: `KRANDOM_JUNIT_RECIPE=base64:... ./gradlew test --tests ... --rerun`;
  - a `@ParameterizedTest` whose `@MethodSource` factory generates its arguments from the class-scoped
    configuration, which the command restores too:
    `KRANDOM_JUNIT_CLASS_RECIPE='...=base64:...' KRANDOM_JUNIT_RECIPE=base64:... ./gradlew test --tests ... --rerun`

It is executed by `../../scripts/verify_examples_local.sh` after the repository modules are published to Maven local.

```bash
../../gradlew -p . -PkrandomVersion=3.1.0-SNAPSHOT test
```
