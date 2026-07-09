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
import io.github.frikit.krandom.generator.Generators
import io.github.frikit.krandom.kotest.toArb
import io.kotest.property.checkAll

val emailArb = Generators.ofEmail().toArb()

checkAll(emailArb) { email ->
    require(email.contains("@"))
}
```

For object generation:

```kotlin
import io.github.frikit.krandom.generator.GeneratorConfig
import io.github.frikit.krandom.kotest.krandomObjectArb

val userArb = krandomObjectArb<UserDto>(
    GeneratorConfig.builder().seed(42L).build()
)
```

The module depends on `krandom-core` transitively.
