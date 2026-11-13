# Java + Gradle Example (Test-based)

Uses `io.github.frikit:krandom-core:3.2.0-SNAPSHOT` by default for repo-local verification.

Use the embedded snapshot default only for repo-local Maven-local checks. For published versions, pass the target version explicitly.

## What it shows

- `JavaGradleUsageTest`: the `Generators` factories, seeded determinism, a typed `ObjectModel` with
  `strict()`, locale data scoped to one `GeneratorConfig` (below), and object-generation options:
  independent field streams, a snapshot clock that the replay recipe keeps, and a failed `ObjectFaker`
  profile that leaves no rule behind.
- `FixtureDiagnosticsExampleTest`: `ObjectFaker.strict()` rejects a shadowed rule with a
  `FixtureRuleException` before anything is generated, and its report names the rule and its path,
  never a value.
- `GenerationReportExampleTest`: a `GenerationReportListener` set on the `GeneratorConfig` receives an
  entry for each generated member, and the test asserts on an entry's path, step, generator and matched
  rule, never on a value.
- `SealedTypeExampleTest`: a sealed interface, as a member and as the root, is generated as one of its
  permitted subclasses, with no subtype mapping.

## Run

```bash
./gradlew :bom:publishToMavenLocal :core:publishToMavenLocal
cd examples/java-gradle
./gradlew test
```

To use a different artifact version:

```bash
./gradlew -PkrandomVersion=<version> test
```

The tests compile and run on Java 21. To run them on another Java, such as 17, the oldest that
`krandom-core` runs on, name it; Gradle must detect a JDK of that version (`./gradlew javaToolchains`):

```bash
./gradlew -PjavaVersion=17 test
```

## Scoped locale overrides

Keep application-specific locale data on `GeneratorConfig` instead of calling a static registry
mutation method. The test suite includes the same working example:

```java
DataRegistryContext data = DataRegistryContext.builder()
    .isolated()
    .register(Vocabulary.WEATHER, customWeather)
    .build();

GeneratorConfig config = GeneratorConfig.builder()
    .locale(Locale.US)
    .registryContext(data)
    .build();

String condition = new WeatherGenerator(config).generate();
```
