# Migrating From EasyRandom To krandom

Easy Random (`org.jeasy.random`) generates structural random object graphs and describes itself as
maintenance-only. kRandom requires Java 21 and adds semantic, locale-aware fixture values. Treat migration as a
behavior change rather than expecting identical seeded objects.

> EasyRandom's `EasyRandomParameters` / `FieldPredicates` / `Randomizer` API is
> the same surface as the k-random reference library. The exhaustive randomizer
> mapping table in [`k-random-to-krandom.md`](./k-random-to-krandom.md) applies
> here too — this guide covers the common cases.

## Why switch

**Pros**: maintained; realistic locale-aware values instead of synthetic noise;
50 supported locale variants (35 native datasets and 15 curated fallbacks); native Bean Validation;
schema export; Spring Boot / JUnit 5 / kotest.
**Cons / gaps**: see the [`competitive landscape`](../competitive-landscape.md).

## Dependency

```kotlin
dependencies {
    testImplementation("io.github.frikit:krandom-core:2.3.0")
}
```

## Entry-point mapping

| EasyRandom | krandom |
|---|---|
| `new EasyRandom()` | `Generators.ofObject(X.class)` |
| `new EasyRandom(params)` | `Generators.ofObject(X.class, config)` |
| `random.nextObject(User.class)` | `Generators.ofObject(User.class).generate()` |
| `random.objects(User.class, 10)` | `Generators.ofObject(User.class).generateList(10)` |

## Parameters mapping

| `EasyRandomParameters` | `GeneratorConfig.builder()` |
|---|---|
| `.seed(42L)` | `.seed(42L)` |
| `.stringLengthRange(min, max)` | `.stringLength(min, max)` |
| `.collectionSizeRange(min, max)` | `.collectionSize(min, max)` |
| `.randomizationDepth(d)` | `.objectMaxDepth(d)` |
| `.objectPoolSize(n)` | `.objectPoolSize(n)` |
| `.ignoreRandomizationErrors(true)` | `.objectIgnoreErrors(true)` |
| `.excludeField(named("password"))` | `.objectExcludeField("password")` |
| `.randomize(String.class, () -> "x")` | `.objectOverride(String.class, () -> "x")` |
| `.randomize(named("email"), () -> v)` | `.objectOverride(FieldPredicates.nameMatches("email"), () -> v)` |
| `.randomize(PaymentMethod.class, CardPayment::new)` | `.objectOverride(PaymentMethod.class, CardPayment::new)` |

`FieldPredicates` / `TypePredicates` keep the same names
(`named/ofType/inClass/isAnnotatedWith/...`).

## Examples

EasyRandom:

```java
EasyRandomParameters params = new EasyRandomParameters()
    .seed(42L)
    .stringLengthRange(3, 16)
    .collectionSizeRange(1, 5)
    .excludeField(FieldPredicates.named("password"));
User user = new EasyRandom(params).nextObject(User.class);
```

krandom (same shape, but `email`/`city`/… come out realistic, not `"asdlkfj"`):

```java
GeneratorConfig config = GeneratorConfig.builder()
    .seed(42L)
    .stringLength(3, 16)
    .collectionSize(1, 5)
    .objectExcludeField("password")
    .build();
User user = Generators.ofObject(User.class, config).generate();
```

## Defaults differ

EasyRandom: string length `1..32`, collection size `1..100`. krandom: string
length `5..20`, collection size `1..10`, bounded object depth. Set these
explicitly during migration if your tests depended on the old defaults.

## Determinism

A seed is scoped to its own library; EasyRandom's exact snapshots are not
reproduced. The same krandom version + `GeneratorConfig` + call order is
repeatable. See the full [`k-random mapping`](./k-random-to-krandom.md) and the
[`product roadmap`](../development/market-leadership-roadmap.md) for tracked gaps.
