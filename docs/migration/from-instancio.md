# Migrating From Instancio To krandom

Instancio is the modern object-graph generator. krandom does the same one-call
graph generation **and** fills fields with realistic, locale-aware values —
Instancio deliberately does not ("realism is not its goal"). Full feature matrix:
[`../feature-parity/instancio-parity.md`](../feature-parity/instancio-parity.md).

## Why switch / trade-offs

**Pros of krandom**: realistic localized data built in (no DataFaker side-car),
35 locales, checksum national IDs, schema export, Spring Boot / Jackson / kotest.
**What Instancio does better today** (tracked in [`GAP-TRACKER.md`](../feature-parity/GAP-TRACKER.md)):
type-safe method-reference selectors (`field(X::getY)`), reusable `Model<T>`,
conditional `assign(when/then)`, and data feeds. krandom uses string/dotted-path
field names and `profile(..)` templates instead.

## Dependency

```kotlin
dependencies {
    testImplementation("io.github.frikit:krandom-core:1.4.0")
    // optional: testImplementation("io.github.frikit:krandom-junit:1.4.0")
}
```

## API mapping

| Instancio | krandom (`ObjectFaker`) |
|---|---|
| `Instancio.create(X.class)` | `new ObjectFaker<>(X.class).generate()` |
| `Instancio.of(X.class)....create()` | `new ObjectFaker<>(X.class, cfg)....generate()` |
| `Instancio.ofList(X).size(n).create()` | `Generators.ofObject(X.class).generateList(n)` |
| `Instancio.stream(X).limit(n)` | `Generators.ofObject(X.class).stream().limit(n)` |
| `set(field(X::getColor), "White")` | `.ruleFor("color", () -> "White")` |
| `supply(field(X::getName), () -> v)` | `.ruleFor("name", () -> v)` |
| `generate(field(P::getAge), g -> g.ints().range(4,50))` | `.ruleFor("age", Generators.ofInt(4, 50))` |
| `generate(field(P::getDob), g -> g.temporal().localDate().past())` | `.ruleFor("dob", Generators.ofLocalDate())` (bounded via `DateGenerator.past()`) |
| `ignore(field(X::getId))` | `.ignore("id")` |
| `withNullable(field(X::getMiddle))` | nullable generator / `objectOptionalEmptyProbability(..)` |
| `subtype(all(AbstractAddress.class), AddressImpl.class)` | `GeneratorConfig.builder().objectSubtype(AbstractAddress.class, AddressImpl.class)` |
| `onComplete(all(P.class), p -> ...)` | `.afterGenerate(p -> ...)` / `.postProcess(op)` |
| `toModel()` + reuse | `.profile("name", f -> ...)` + `.useProfile("name")` |
| nested target `field(Address::getCity)` | dotted path `.ruleFor("address.city", ...)` |
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
    .ruleFor("lastName", () -> "Simpson")
    .ruleFor("age", Generators.ofInt(18, 65))
    .ruleFor("email", Generators.ofEmail(cfg))     // realistic, locale-aware, in-library
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

Honest gaps and roadmap:
[`../feature-parity/instancio-parity.md`](../feature-parity/instancio-parity.md) ·
[`../feature-parity/GAP-TRACKER.md`](../feature-parity/GAP-TRACKER.md).
