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

The module depends on `krandom-core` transitively. It does not provide structural shrinkers for
arbitrary object fixtures; use Kotest's native primitive/selection `Arb`s when shrinking is needed.
