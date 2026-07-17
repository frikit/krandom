---
layout: page
title: Migration from k-random
permalink: /guides/k-random-migration/
---

# Migration from k-random

Use this mapping to move from `io.github.k-random:k-random-*` artifacts to native kRandom APIs. This is feature-parity guidance, not source-compatible import replacement.

## Install

Gradle Kotlin:

```kotlin
dependencies {
    implementation("io.github.frikit:krandom-core:2.0.0")
}
```

Maven:

```xml
<dependency>
  <groupId>io.github.frikit</groupId>
  <artifactId>krandom-core</artifactId>
  <version>2.0.0</version>
</dependency>
```

## API mapping

| k-random | kRandom equivalent |
|---|---|
| `new KRandom().nextObject(User.class)` | `Generators.ofObject(User.class).generate()` |
| `random.objects(User.class, 10)` | `Generators.ofObject(User.class).generateList(10)` |
| `new KRandomParameters().seed(42L)` | `GeneratorConfig.builder().seed(42L)` |
| `stringLengthRange(min, max)` | `stringLength(min, max)` |
| `collectionSizeRange(min, max)` | `collectionSize(min, max)` |
| `randomizationDepth(depth)` | `objectMaxDepth(depth)` |
| `randomize(type, randomizer)` | `objectOverride(type, generator)` |
| `randomize(fieldPredicate, randomizer)` | `objectOverride(fieldPredicate, generator)` |
| `excludeField(fieldPredicate)` | `objectExclude(fieldPredicate)` / `objectExcludeField(name)` |
| `@io.github.krandom.annotation.Randomizer` | `@io.github.frikit.krandom.generator.object.Randomizer` |
| `k-random-bean-validation` | Native Bean Validation handling in `krandom-core` |

## Before and after

k-random:

```java
KRandomParameters parameters = new KRandomParameters()
    .seed(42L)
    .randomize(FieldPredicates.named("email"), () -> "owner@example.test")
    .excludeField(FieldPredicates.named("password"));

User user = new KRandom(parameters).nextObject(User.class);
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

## Domain generators

k-random faker/DataFaker randomizers map to kRandom facade methods and fluent namespaces:

```java
String firstName = Generators.person().firstName().generate();
String email = Generators.ofEmail().generate();
String city = Generators.location().city().generate();
```

The complete migration guide lives in the repository at [docs/migration/k-random-to-krandom.md](https://github.com/frikit/krandom/blob/main/docs/migration/k-random-to-krandom.md).
