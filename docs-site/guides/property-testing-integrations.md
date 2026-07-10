---
layout: page
title: Property Testing Integrations
permalink: /guides/property-testing-integrations/
---

# Property Testing Integrations

Use the property-testing module when kRandom generators should feed Kotest test data.

## Kotest

Dependency:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    testImplementation("io.github.frikit:krandom-kotest-extensions:1.5.0")
}
```

Usage:

```kotlin
import io.github.frikit.krandom.generator.GeneratorConfig
import io.github.frikit.krandom.generator.user.EmailGenerator
import io.github.frikit.krandom.kotest.krandomArb
import io.kotest.property.checkAll

val emailArb = krandomArb(GeneratorConfig.defaults()) { config ->
    EmailGenerator(config)
}

checkAll(emailArb) { email ->
    require(email.contains("@"))
}
```

`krandomArb(config) { ... }` creates a fresh generator for every Kotest random-source draw. A
failing Kotest seed therefore reproduces the same fixture sequence without sharing mutable
kRandom generator state between cases. The older `Generator.toArb()` and no-argument
`krandomArb { ... }` bridges are deprecated: they reuse one mutable generator and are unsuitable
for host-controlled replay or parallel property tests.

For object generation:

```kotlin
import io.github.frikit.krandom.generator.GeneratorConfig
import io.github.frikit.krandom.kotest.krandomReplayObjectArb

val userArb = krandomReplayObjectArb<UserDto>(
    GeneratorConfig.builder().seed(42L).build()
)
```

## Shrinking for bounded primitives and selections

Bounded primitives and list selections have shrinking-aware adapters:

```kotlin
import io.github.frikit.krandom.kotest.krandomIntArb
import io.github.frikit.krandom.kotest.krandomPickArb

checkAll(krandomIntArb(0, 1000)) { value ->
    require(value < 1000)
}

checkAll(krandomPickArb(listOf("basic", "premium", "enterprise"))) { plan ->
    require(plan.isNotEmpty())
}
```

- `krandomIntArb(min, max)`, `krandomLongArb(min, max)`, and `krandomDoubleArb(min, max)` generate
  kRandom's half-open `[min, max)` range, expose the attainable bounds (plus `-1`, `0`, and `1`
  when inside the range) as Kotest edge cases, and shrink only to in-range values.
- `krandomPickArb(source)` picks one element, uses the first element as the edge case, and shrinks
  toward elements earlier in `source`.

Kotest's `RandomSource` owns determinism for these adapters: replaying a failing Kotest seed
reproduces the same values, and a `GeneratorConfig` seed is deliberately not consulted.

## Types that cannot be structurally shrunk

Object fixtures (`krandomReplayObjectArb`), semantic values such as emails, names, addresses, and
identifiers, and any `krandomArb` factory output have no structural shrinker: kRandom generates
them as opaque values, so there is no meaningful "smaller" fixture to propose. On failure these
adapters rely on Kotest seed replay instead of shrinking.

The module depends on `krandom-core` transitively.
