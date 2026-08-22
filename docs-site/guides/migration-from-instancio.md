---
layout: page
title: Migration from Instancio
permalink: /guides/migration-from-instancio/
---

# Migration from Instancio

Use this mapping to move Instancio fixture code to kRandom. The two libraries
share the object-population core but differ in philosophy: Instancio is a
fixture DSL with selectors and models; kRandom pairs reflection-based
population with semantic data and explicit rules. The common cases map
directly; the honest-gaps section lists what does not (yet).

## Install

```kotlin
dependencies {
    implementation("io.github.frikit:krandom-core:2.0.0")
}
```

## API mapping

| Instancio | kRandom equivalent |
|---|---|
| `Instancio.create(User.class)` | `Generators.ofObject(User.class).generate()` |
| `Instancio.ofList(User.class).size(10).create()` | `Generators.ofObject(User.class).generateList(10)` |
| `Instancio.of(User.class).set(field(User::getEmail), v).create()` | `ObjectFaker.of(User.class).ruleFor(User::getEmail, () -> v).generate()` |
| `.supply(field(User::getId), gen)` | `.ruleFor(User::getId, gen)` (any `Generator<T>` or lambda) |
| `.ignore(field(User::getPassword))` | `.ignore(User::getPassword)` / `.objectExcludeField("password")` |
| `.withNullable(field(...))` | `GeneratorConfig.builder().objectNullProbability(0.1)` |
| `.onComplete(...)` | `ObjectFaker.afterGenerate(consumer)` / `.postProcess(operator)` |
| `.withSeed(42)` | `GeneratorConfig.builder().seed(42L)` |
| `.withMaxDepth(3)` | `.objectMaxDepth(3)` |
| `Instancio.of(Model<T>)` | `ObjectModel<T>.faker()` / `.generate()`; compose models with `.and(...)` |
| `generate(field(...), gen -> gen.ints().range(1, 10))` | `.ruleFor("count", Generators.ofInt(1, 10))` |
| `@ExtendWith(InstancioExtension.class)` + `@Seed(42)` | `@ExtendWith(KrandomExtension.class)` + `@KrandomSeed(42L)` from `krandom-junit` — same failure-seed reporting; see the [JUnit Extension]({{ '/guides/junit-extension/' | relative_url }}) guide |

## Before and after

Instancio:

```java
User user = Instancio.of(User.class)
    .set(field(User::getEmail), "owner@example.test")
    .ignore(field(User::getPassword))
    .withSeed(42)
    .create();
```

kRandom:

```java
GeneratorConfig config = GeneratorConfig.builder().seed(42L).build();

User user = new ObjectFaker<>(User.class, config)
    .ruleFor(User::getEmail, () -> "owner@example.test")
    .ignore(User::getPassword)
    .generate();
```

## What kRandom adds over Instancio

- **Semantic field values** — `firstName`, `email`, `city` resolve to
  realistic domain data, not arbitrary strings (this is the documented
  throughput-for-realism trade-off; see the README performance section).
- **Bean Validation awareness** — 21 Jakarta constraints (`@Email`, `@Min`,
  `@Pattern`, `@Size`, …) are honored during population without extra setup.
- **Bulk structured export** — `Field` + `Schema` produce CSV, JSONL, XML,
  and SQL output; Instancio has no export story.
- **Locale-aware data** across 50 supported locale variants (35 native datasets and 15 curated
  fallbacks).

## Honest gaps

- **Feeds** (CSV/JSON-backed data sources): not shipped; on the roadmap.
- **Selector depth scoping** (`atDepth(...)`): use `objectMaxDepth` plus
  per-field rules; per-depth selectors have no direct equivalent.
- **Selector groups and arbitrary selector predicates**: typed paths select one property at a
  time; broad predicate rules remain configured through `GeneratorConfig` field predicates.
- **JPA-specific constraints**: Bean Validation is native, but JPA metadata such as
  `@Column(length=...)` is not interpreted.
