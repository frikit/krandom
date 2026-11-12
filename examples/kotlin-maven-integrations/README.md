# Kotlin Maven Integration Modules Example

This example verifies that published integration artifacts are consumable from a clean Kotlin Maven build:

- `io.github.frikit:krandom-kotest-extensions` — a `krandomArb` Kotest arbitrary
- `io.github.frikit:krandom-kotlin-dsl` — `krandom` and `krandomList` with rules, independent object-field
  streams, and, in `SealedAndValueClassExampleTest`, a sealed class generated as one of its subclasses and
  a `@JvmInline` value class, whose `init` block checks the email address generated for it
- `io.github.frikit:krandom-spring-boot-starter` — Spring Boot auto-configuration from Kotlin, and
  `krandom.*` properties (seed, locale, clock and its zone, object depth, collection sizes, and a safety
  policy) bound into the `GeneratorConfig`
- `io.github.frikit:krandom-jackson` — on Jackson 3 (`tools.jackson`), a mapper built with
  `KrandomJackson.configure(JsonMapper.builder())` writes a `Schema` as JSON Schema and a generated row,
  whose `java.time` order date is ISO-8601 text

It is executed by `../../scripts/verify_examples_local.sh` after the repository modules are published to Maven local.

```bash
mvn -Dkrandom.version=3.0.2-SNAPSHOT test
```
