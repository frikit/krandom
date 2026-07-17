---
layout: page
title: Migration from easy-random
permalink: /guides/migration-from-easy-random/
---

# Migration from easy-random

Use this mapping to move easy-random (org.jeasy) code to kRandom. kRandom's
object generation covers the easy-random core feature set natively, including
Bean Validation handling that easy-random ships as a separate module.

## Install

```kotlin
dependencies {
    implementation("io.github.frikit:krandom-core:2.0.0")
}
```

## API mapping

| easy-random | kRandom equivalent |
|---|---|
| `new EasyRandom().nextObject(User.class)` | `Generators.ofObject(User.class).generate()` |
| `easyRandom.objects(User.class, 10)` | `Generators.ofObject(User.class).generateList(10)` |
| `easyRandom.objects(User.class, 10).stream()` | `Generators.ofObject(User.class).stream().limit(10)` |
| `new EasyRandomParameters()` | `GeneratorConfig.builder()` |
| `.seed(42L)` | `.seed(42L)` |
| `.stringLengthRange(3, 16)` | `.stringLength(3, 16)` |
| `.collectionSizeRange(1, 5)` | `.collectionSize(1, 5)` |
| `.randomizationDepth(3)` | `.objectMaxDepth(3)` |
| `.dateRange(min, max)` | `.objectDateRange(min, max)` |
| `.randomize(String.class, randomizer)` | `.objectOverride(String.class, generator)` |
| `.randomize(named("email"), randomizer)` | `.objectOverride(User.class, "email", generator)` or `.objectOverride(fieldPredicate, generator)` |
| `.excludeField(named("password"))` | `.objectExcludeField("password")` or `.objectExclude(fieldPredicate)` |
| `.excludeType(t -> ...)` | `.objectExcludeType(...)` |
| `.scanClasspathForConcreteTypes(true)` | `.objectSubtype(declaredType, implementationType)` per abstract type |
| `@org.jeasy.random.annotation.Randomizer` | `@io.github.frikit.krandom.generator.object.Randomizer` |
| `@org.jeasy.random.annotation.Exclude` | `@io.github.frikit.krandom.generator.object.Exclude` |
| `easy-random-bean-validation` module | built into `krandom-core` (21 Jakarta constraints honored) |

## Before and after

easy-random:

```java
EasyRandomParameters parameters = new EasyRandomParameters()
    .seed(42L)
    .randomize(FieldPredicates.named("email"), () -> "owner@example.test")
    .excludeField(FieldPredicates.named("password"));

User user = new EasyRandom(parameters).nextObject(User.class);
```

kRandom:

```java
GeneratorConfig config = GeneratorConfig.builder()
    .seed(42L)
    .objectOverride(User.class, "email", () -> "owner@example.test")
    .objectExcludeField("password")
    .build();

User user = Generators.ofObject(User.class, config).generate();
```

## What kRandom does differently (on purpose)

- **Semantic field values.** Where easy-random fills `firstName` with random
  characters, kRandom resolves common field names to realistic values (real
  names, valid emails, real cities) via the semantic field registry. Set
  `objectSemanticRegistry` to extend or disable per project.
- **Records and constructor-less classes** are handled natively (canonical
  constructor for records, Objenesis fallback for classes without a no-arg
  constructor).
- **Cycle handling** uses an object pool rather than easy-random's depth
  cutoff alone — circular references resolve to cached instances.

## Honest gaps

- **`scanClasspathForConcreteTypes(true)`**: kRandom does not scan the
  classpath to pick implementations for abstract/interface fields. Map each
  implementation explicitly instead:
  `.objectSubtype(PaymentMethod.class, CardPayment.class)` — the mapped
  implementation is then fully populated like any concrete type.
- **ServiceLoader randomizer SPI**: kRandom uses explicit `ProviderHub`
  registration instead of `META-INF/services` discovery.
- **Setter-based population**: kRandom populates via field reflection, not
  setters. Logic inside setters will not run during generation; use
  `ObjectFaker.postProcess(...)` when you need it.
