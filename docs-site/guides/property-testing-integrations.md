---
layout: page
title: Property Testing Integrations
permalink: /guides/property-testing-integrations/
---

# Property Testing Integrations

Use the property-testing modules when kRandom generators should feed Kotest or jqwik test data.

## Kotest

Dependency:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    testImplementation("io.github.frikit:krandom-kotest-extensions:1.0.0")
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

## jqwik

Dependency:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    testImplementation("io.github.frikit:krandom-jqwik-extensions:1.0.0")
}
```

Usage:

```java
import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.jqwik.KrandomArbitraries;
import net.jqwik.api.Arbitrary;

Arbitrary<String> emails = KrandomArbitraries.fromGenerator(Generators.ofEmail());
```

For generated objects:

```java
Arbitrary<UserDto> users = KrandomArbitraries.forType(UserDto.class);
```

Both modules depend on `krandom-core` transitively.
