# Migrating From JavaFaker To krandom

The original JavaFaker repository still documents `com.github.javafaker:javafaker:1.0.2` and has
been superseded for active Faker-style development by DataFaker. If you are still on JavaFaker,
treat migration as a dependency refresh rather than extending the legacy surface.

Two paths:
1. **JavaFaker → DataFaker** — a near drop-in fork (same `faker.name().fullName()`
   API), if you only need realistic field values.
2. **JavaFaker → krandom** — if you also want one-call object/record graphs,
   reproducible seeds, and Bean-Validation-aware generation. This is the path
   below.

## Why switch to krandom

**Pros**: maintained; realistic field data **and** object-graph generation in one
library; 50 supported locale variants (35 native datasets and 15 curated fallbacks); schema export;
Spring Boot / JUnit 5 / kotest integrations.
**Cons / gaps**: kRandom intentionally omits novelty and fandom catalogs from core; use DataFaker
or a local, provenance-declared data pack when those datasets are required.

## Dependency

```kotlin
dependencies {
    testImplementation("io.github.frikit:krandom-core:2.2.0")
}
```

## Mapping

JavaFaker's provider API is the same shape as DataFaker's, so the field-level
mapping in [`from-datafaker.md`](./from-datafaker.md) applies verbatim. The most
common calls:

| JavaFaker | krandom |
|---|---|
| `new Faker()` / `new Faker(Locale.UK)` | `GeneratorConfig.builder().locale(Locale.UK).seed(1L).build()` |
| `faker.name().fullName()` | `Generators.ofFullName(cfg).generate()` |
| `faker.name().firstName()` / `lastName()` | `Generators.person(cfg).firstName().generate()` / `lastName()` |
| `faker.internet().emailAddress()` | `Generators.ofEmail(cfg).generate()` |
| `faker.address().city()` / `streetAddress()` | `Generators.location(cfg).city().generate()` / `streetAddress()` |
| `faker.phoneNumber().cellPhone()` | `Generators.ofPhoneNumber(cfg).generate()` |
| `faker.bothify("???-###")` / `numerify` / `letterify` | `Generators.ofTemplate("???-###").generate()` |
| `faker.regexify("...")` | `Generators.ofRegex("...").generate()` |

## Example

JavaFaker:

```java
Faker faker = new Faker();
String name  = faker.name().fullName();
String email = faker.internet().emailAddress();
```

krandom (and you can now fill a whole object, which JavaFaker never did):

```java
GeneratorConfig cfg = GeneratorConfig.builder().seed(7L).build();
String name  = Generators.ofFullName(cfg).generate();
String email = Generators.ofEmail(cfg).generate();

User user = new ObjectFaker<>(User.class, cfg).generate();   // not possible in JavaFaker
```

## Determinism

A seed is scoped to its own library; JavaFaker's exact strings are not
reproduced. The same krandom version + `GeneratorConfig` + call order is
repeatable.

For the field-level mapping and tracked priorities see
[`from-datafaker.md`](./from-datafaker.md) and the
[`competitive landscape`](../competitive-landscape.md).
