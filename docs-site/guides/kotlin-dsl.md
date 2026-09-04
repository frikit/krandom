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
    testImplementation("io.github.frikit:krandom-kotlin-dsl:2.3.0")
}
```

## Generate One Object

```kotlin
import io.github.frikit.krandom.dsl.krandom

val user = krandom<UserDto> {
    config { seed(42L) }
    rule(UserDto::firstName) { "Ada" }
    rule(UserDto::email) { "ada@example.test" }
}
```

## Typed Rules and Validation

Prefer property references over string names: they survive renames, and the compiler checks that
the rule value matches the property type. The string form remains as a compatibility bridge for
fields that cannot be referenced as Kotlin properties.

Rules are validated before generation:

- registering two rules for the same field or the same type fails immediately;
- a rule naming a field that does not exist on the target class fails when the generator is
  built, listing the known field names.

```kotlin
val account = krandom<Account> {
    rule(Account::owner) { "grace" }   // type-safe; survives refactoring
    exclude(Account::internalNotes)    // type-safe exclusion
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

## Immutable Data Classes, Generics, Defaults, and Constraints

```kotlin
// Immutable data classes construct through the primary constructor
data class Account(val owner: String, val balance: Long)
val account = krandom<Account> { rule(Account::owner) { "grace" } }

// Nested generics keep their declared element types
data class Ledger(val entries: List<Map<String, Long>>)
val ledger = krandom<Ledger>()

// Kotlin default parameter values are honored unless a rule overrides them
data class Invoice(val currency: String = "EUR", val amount: Long)
val invoice = krandom<Invoice>()          // currency == "EUR"

// Jakarta Validation constraints on constructor parameters are satisfied
data class Customer(@field:jakarta.validation.constraints.Email val contact: String)
val customer = krandom<Customer>()
```

## Standalone Configuration and Replay Recipes

```kotlin
val config = krandomConfig {
    seed("checkout-fixtures")   // textual seeds use the shared derivation contract
    constructionPolicy(ObjectConstructionPolicy.SAFE)
}
val recipe = config.generationRecipe.orElseThrow().serialize()  // portable replay recipe
```

The DSL matches the Java builder defaults with one intentional, documented difference:
`objectOverrideDefaultInitialization` is enabled so `rule(...)` replaces property initializers.

## Opt-in independent streams (2.3+)

```kotlin
import io.github.frikit.krandom.generator.`object`.ObjectFieldStreamPolicy

val users = krandomList<User>(5) {
    config {
        seed(42L)
        objectFieldStreamPolicy(ObjectFieldStreamPolicy.INDEPENDENT)
    }
    rule(User::name) { "Ada" }
}
```

The Kotlin option uses the same core policy as Java. Existing configurations remain LEGACY. For direct configuration
consumers, `krandomConfig { seed(42L) }.snapshotClock()` captures an explicit replay clock.
