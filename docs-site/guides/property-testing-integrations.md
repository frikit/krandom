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
    testImplementation("io.github.frikit:krandom-kotest-extensions:2.5.0")
}
```

The module brings `kotest-property` transitively at the Kotest version it is built and tested with;
keep your other Kotest modules on that same version.

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
kRandom generator state between cases. The legacy `Generator.toArb()`, no-argument
`krandomArb { ... }`, and `krandomObjectArb` bridges are removed in 2.0.0: they reused one mutable
generator and could not support host-controlled replay or parallel property tests.

For object generation:

```kotlin
import io.github.frikit.krandom.generator.GeneratorConfig
import io.github.frikit.krandom.kotest.krandomReplayObjectArb

val userArb = krandomReplayObjectArb<UserDto>(
    GeneratorConfig.builder().seed(42L).build()
)
```

The configuration's seed parents every sample seed (2.6+): for the same Kotest seed, `seed(42L)`
and `seed(43L)` produce different, individually reproducible streams, and an unseeded
configuration behaves like `seed(0L)`. Kotest's seed still selects the samples, so replay needs
both the Kotest seed and the configuration (or its recipe, which records the seed).

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

## Failure Recipes and Supported Versions

`checkAllWithRecipe(config, arb) { ... }` rethrows a failing property with the portable kRandom
recipe of the configuration appended below Kotest's own seed report, so a CI failure carries both
replay halves; `krandomKotestRecipe(config)` returns the same value-free recipe directly. That
recipe reads the configuration's clock when the failure is reported, and the appended header says
so; use the overload below for time-sensitive properties.

## Temporal replay with one snapshot (2.3+)

Let `checkAllWithRecipe` take the snapshot (2.6+): pass an Arb factory instead of an Arb. The
clock of `config` is captured once with `snapshotClock()`, the factory builds the Arb from that
snapshot, and the failure recipe records the same instant:

```kotlin
checkAllWithRecipe(GeneratorConfig.builder().seed(42L).build(), { session ->
    krandomArb(session) { sample -> Generator { DateGenerator(sample).future(7) } }
}) { date ->
    // Assert the application contract here.
}
```

An optional `PropTestConfig` argument pins Kotest's seed or iteration count on replay, for example
`checkAllWithRecipe(config, factory, PropTestConfig(seed = 1234L)) { ... }`.

With the Arb-based overload, build the configuration once with `snapshotClock()` before creating
the Arb, and pass the same configuration to `checkAllWithRecipe`:

```kotlin
val session = GeneratorConfig.defaults().snapshotClock()
val arb = krandomArb(session) { sample ->
    Generator { DateGenerator(sample).future(7) }
}
checkAllWithRecipe(session, arb) { date ->
    // Assert the application contract here.
}
```

The snapshot is shared across property samples. Kotest's seed controls sample randomness, while
the kRandom recipe records configuration including the captured clock. Snapshotting only in the
failure handler or passing a different configuration to the Arb cannot reproduce temporal data.
