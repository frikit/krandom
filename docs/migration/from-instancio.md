# Migrating From Instancio To krandom

Instancio is a mature object-graph generator. kRandom also provides one-call graph generation and
adds built-in realistic, locale-aware values.

## Why switch / trade-offs

**Pros of krandom**: realistic localized data built in (no DataFaker side-car),
50 supported locale variants (35 native datasets and 15 curated fallbacks), checksum national IDs,
schema export, Spring Boot / Jackson / kotest.
**What Instancio does better today**: a broader selector vocabulary and scoping model, element and
setter selectors, conditional assignment, data feeds, and generated field/parameter injection for
JUnit. kRandom 2.1 already provides Java method-reference selectors, typed nested paths, immutable
`ObjectModel<T>` templates, dependent rules, and strict unused-rule validation; the remaining gaps
are tracked in the [`product roadmap`](../development/market-leadership-roadmap.md).

## Dependency

```kotlin
dependencies {
    testImplementation("io.github.frikit:krandom-core:2.4.0")
    // optional: testImplementation("io.github.frikit:krandom-junit:2.4.0")
}
```

## API mapping

| Instancio | krandom (`ObjectFaker`) |
|---|---|
| `Instancio.create(X.class)` | `new ObjectFaker<>(X.class).generate()` |
| `Instancio.of(X.class)....create()` | `new ObjectFaker<>(X.class, cfg)....generate()` |
| `Instancio.ofList(X).size(n).create()` | `Generators.ofObject(X.class).generateList(n)` |
| `Instancio.stream(X).limit(n)` | `Generators.ofObject(X.class).stream().limit(n)` |
| `set(field(X::getColor), "White")` | `.ruleFor(X::getColor, () -> "White")` |
| `supply(field(X::getName), () -> v)` | `.ruleFor(X::getName, () -> v)` |
| `generate(field(P::getAge), g -> g.ints().range(4,50))` | `.ruleFor(P::getAge, Generators.ofInt(4, 50))` |
| `generate(field(P::getDob), g -> g.temporal().localDate().past())` | `.ruleFor(P::getDob, Generators.ofLocalDate())` (bounded via `DateGenerator.past()`) |
| `ignore(field(X::getId))` | `.ignore(X::getId)` |
| `withNullable(field(X::getMiddle))` | nullable generator / `objectOptionalEmptyProbability(..)` |
| `subtype(all(AbstractAddress.class), AddressImpl.class)` | `GeneratorConfig.builder().objectSubtype(AbstractAddress.class, AddressImpl.class)` |
| `onComplete(all(P.class), p -> ...)` | `.afterGenerate(p -> ...)` / `.postProcess(op)` |
| `toModel()` + reuse | `ObjectModel.of(X.class).configure(...)`; compose with `.and(...)` |
| nested target `field(Address::getCity)` | `.ruleFor(PropertyPath.of(X::getAddress).then(Address::getCity), ...)` |
| `InstancioExtension` + `@Seed(123)` | `KrandomExtension` + `@KrandomSeed(123)` (`krandom-junit`) |

## Example

Instancio:

```java
Person p = Instancio.of(Person.class)
    .set(field(Person::getLastName), "Simpson")
    .generate(field(Person::getAge), gen -> gen.ints().range(18, 65))
    .supply(field(Person::getEmail), () -> faker.internet().emailAddress())  // needs DataFaker
    .create();
```

krandom (realistic email is built in — no second library):

```java
GeneratorConfig cfg = GeneratorConfig.builder().locale(Locale.US).seed(42L).build();
Person p = new ObjectFaker<>(Person.class, cfg)
    .ruleFor(Person::getLastName, () -> "Simpson")
    .ruleFor(Person::getAge, Generators.ofInt(18, 65))
    .ruleFor(Person::getEmail, Generators.ofEmail(cfg)) // realistic and locale-aware
    .generate();
```

## Bean Validation

Both honor Jakarta Bean Validation during generation
(`@Size/@Min/@Max/@Email/@Pattern/@Past/@Future/@NotBlank/@Positive`…); in
krandom this is native to object generation — no extra module.

## Determinism

A seed is scoped to its own library; Instancio's exact objects are not
reproduced. The same krandom version + `GeneratorConfig` + call order is
repeatable, and `@KrandomSeed` reports/pins the seed for failed tests just like
Instancio's `@Seed`.

Honest gaps and priorities are summarized in the
[`competitive landscape`](../competitive-landscape.md) and
[`product roadmap`](../development/market-leadership-roadmap.md).
