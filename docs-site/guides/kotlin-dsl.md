---
layout: page
title: Kotlin DSL
permalink: /guides/kotlin-dsl/
---

# Kotlin DSL

Use `krandom-kotlin-dsl` when Kotlin tests need compact object-generation rules without dropping into Java builders.

## Dependency

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    testImplementation("io.github.frikit:krandom-kotlin-dsl:<version>")
}
```

## Generate One Object

```kotlin
import io.github.frikit.krandom.dsl.krandom

val user = krandom<UserDto> {
    config { seed(42L) }
    rule("firstName") { "Ada" }
    rule("email") { "ada@example.test" }
}
```

## Generate Lists

```kotlin
import io.github.frikit.krandom.dsl.krandomList

val users = krandomList<UserDto>(10) {
    rule("country") { "United States" }
}
```

## Reusable Generators

```kotlin
import io.github.frikit.krandom.dsl.krandomGenerator

val users = krandomGenerator<UserDto> {
    ruleForType<String> { "fixed" }
}

val one = users.generate()
val many = users.generateList(5)
```

Rules are backed by `GeneratorConfig` object overrides, so the DSL shares the same defaults, seeding, locale, and object-generation behavior as the Java API.
