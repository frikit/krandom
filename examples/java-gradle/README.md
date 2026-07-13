# Java + Gradle Example (Test-based)

Uses `io.github.frikit:krandom-core:2.0.0-SNAPSHOT` by default for repo-local verification.

Use the embedded snapshot default only for repo-local Maven-local checks. For published versions, pass the target version explicitly.

## Run

```bash
./gradlew :core:publishToMavenLocal
cd examples/java-gradle
./gradlew test
```

To use a different artifact version:

```bash
./gradlew -PkrandomVersion=<version> test
```

## Scoped locale overrides

Keep application-specific locale data on `GeneratorConfig` instead of calling a static registry
mutation method. The test suite includes the same working example:

```java
DataRegistryContext data = DataRegistryContext.builder()
    .isolated()
    .registerWeatherProvider(customWeather)
    .build();

GeneratorConfig config = GeneratorConfig.builder()
    .locale(Locale.US)
    .registryContext(data)
    .build();

String condition = new WeatherGenerator(config).generate();
```
