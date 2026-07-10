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
    testImplementation("io.github.frikit:krandom-kotlin-dsl:1.5.0")
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

## Immutable Kotlin classes

`krandom-kotlin-dsl` includes Kotlin reflection and constructs immutable Kotlin values through their
primary constructor. No extra `kotlin-reflect` declaration is needed.

```kotlin
data class UserFixture(
    val name: String,
    val email: String,
    val roles: List<Role>,
    val source: String = "test"
)

data class Role(val code: String)

val user = krandom<UserFixture> {
    rule("name") { "Ada Lovelace" }
    rule("email") { "ada@example.test" }
}

check(user.source == "test") // optional parameters retain their Kotlin default
```

The primary-constructor adapter uses the normal kRandom resolver for each parameter, so field/type
rules, nested generic containers, semantic values, deterministic seeds, and Jakarta Validation
annotations all apply. Both `@param:` and `@field:` constraint targets are recognized. An explicit
field or type rule wins over an optional parameter default.

Kotlin `object` singletons return their one instance. Value classes and sealed/abstract Kotlin
types are rejected before returning a value; use a concrete type or explicit type override. A
core-only application also rejects immutable Kotlin types with a message naming
`krandom-kotlin-dsl`, rather than allocating an invalid instance.

The adapter always invokes the primary constructor; secondary constructors are never selected.
Delegated properties initialize normally as part of that construction. A required recursive
immutable graph fails contextually rather than returning a value with a runtime null.
