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
    testImplementation("io.github.frikit:krandom-kotlin-dsl:2.10.0")
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
the rule value matches the property type. Use the string form only for fields that cannot be
referenced as Kotlin properties.

Rules are validated before generation:

- registering two rules for the same field or the same type fails immediately;
- a rule or exclusion naming a field that does not exist on the target class fails when the
  generator is built, listing the known field names;
- a rule or exclusion on a computed (`val x get() = ...`) or delegated (`by lazy`) property fails
  when the generator is built: such properties have no backing field and are not primary-constructor
  parameters, so nothing could apply to them.

```kotlin
val account = krandom<Account> {
    rule(Account::owner) { "grace" }   // type-safe; survives refactoring
    exclude(Account::internalNotes)    // type-safe exclusion
}
```

Field rules and exclusions are scoped to the generated (root) type (2.6+): `exclude(Customer::name)`
leaves `Customer.name` at its initial value while a nested `Company.name` is still generated. A field
inherited from a superclass is matched through that superclass field, so it also applies to other
subclasses of it in the same object graph. Use `ruleForType` for type-wide rules and the Java
`ObjectFaker` for nested-path rules. An exclusion also applies to a primary-constructor parameter of
an immutable class: an optional parameter keeps its default, a nullable one is `null`, and a required
non-null one fails generation.

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

That includes [unique fields]({{ '/guides/object-generation/' | relative_url }}#unique-fields). A rule or `ruleForType` may repeat a constant on every object, as above. To request a field whose values must not repeat, use `config { objectUniqueFields(...) }`, which replaces the names that are unique by default. A constant on a requested field fails on a second object from the same generator.

## Java fixture API and Kotlin DSL scope

The Kotlin DSL is the concise, typed front end for ordinary root-property fixtures. It deliberately
does not claim every Java `ObjectFaker` feature. Choose the Java fixture API when a test needs the
broader rule-composition surface.

| Capability | Java `ObjectFaker` | Kotlin DSL |
|:---|:---|:---|
| Root property rules | String, accessor, and `PropertyPath` rules | Property-reference and string rules |
| Nested-path rules | Supported | Use Java `ObjectFaker` |
| Contextual and dependent rules | Supported | Use Java `ObjectFaker` |
| Include, profiles, post-processing, and `populate` | Supported | Use Java `ObjectFaker` |
| Immutable reusable models | `ObjectModel<T>` | Use Java `ObjectModel` |
| Kotlin primary constructors | Via the Kotlin DSL module | Via the Kotlin DSL module |
| Initialized properties | A field rule replaces them; other values keep them unless `objectOverrideDefaultInitialization(true)` | Rules, `ruleForType` values, and generated values all replace them |

In both APIs, an explicit field rule takes precedence over a type rule and generated defaults.
The DSL validates duplicate field/type rules immediately (a Java `ObjectFaker` rule instead replaces
an earlier rule for the same path) and unknown root field names when building.
It generates [sealed types and value classes](#sealed-types-and-value-classes), while an abstract
class or interface that is not sealed needs a `ruleForType`.
These boundaries are intentional: they keep the Kotlin adapter small while the Java API remains the
single place for complex fixture composition.

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

Kotlin `object` singletons return their one instance, and sealed types and value classes are generated
as [below](#sealed-types-and-value-classes). An abstract class or interface that is not sealed is
rejected before returning a value; use a concrete type or a `ruleForType`. A core-only application
also rejects immutable Kotlin types with a message naming `krandom-kotlin-dsl`, rather than allocating
an invalid instance.

The adapter always invokes the primary constructor, whatever its visibility, as the object engine
invokes a Java constructor; secondary constructors are never selected. A factory function that
validates outside the constructor's `init` block is therefore not called, so register a rule for such
a type. Delegated properties initialize normally as part of that construction. A required recursive
immutable graph fails contextually rather than returning a value with a runtime null.

## Sealed types and value classes

A sealed class or interface, as the generated type or as a property, is generated as one of the classes
of its hierarchy. The adapter reads them from the Kotlin metadata, whatever JVM target compiled them:

```kotlin
sealed interface Payment
data class Card(val number: String) : Payment
data class Transfer(val iban: String) : Payment
data object Cash : Payment

data class Order(val payment: Payment)

val order = krandom<Order> { config { seed(42L) } }
// order.payment is a Card, a Transfer, or Cash, and the same one for the same seed
```

Each class is equally likely, and the choice is drawn from the seeded stream in binary-name order. An
`object` gives its one instance, an enum one of its constants, and a sealed subclass its own classes,
at any depth. A class already being generated on the property's path is left out while another
remains, so a recursive hierarchy, such as an expression tree with
`data class Add(val left: Expr, val right: Expr) : Expr`, ends in a leaf rather than failing on a
non-null property. A `ruleForType` for the sealed type wins over the choice, and one for a subclass
generates that subclass when it is chosen. An abstract subclass that is not sealed fails generation,
naming it, whatever the seed, until it has a `ruleForType`, and so does a sealed type with no subclass.
A generic sealed property passes its type arguments on: a `Result<String>` generated as
`data class Ok<T>(val value: T) : Result<T>` holds a `String`. The
[object generation guide]({{ '/guides/object-generation/' | relative_url }}#sealed-types) has the
whole contract, which Java sealed types share.

A `@JvmInline value class` is generated as the value it wraps, and boxed through its primary
constructor, so its `init` block runs. For a constructor parameter, the wrapped value is generated
under the parameter's name, and with the parameter's constraints, or else those declared inside the
value class, outermost first:

```kotlin
@JvmInline value class EmailAddress(val value: String)
@JvmInline value class Pin(@param:Size(min = 4, max = 4) val digits: String)
@JvmInline value class Initials(val letters: String)
@JvmInline value class Cents(val amount: Long)

data class Member(
    val email: EmailAddress,                              // an email address, from the name
    val pin: Pin,                                         // four characters, from Pin's constraint
    @field:Size(min = 2, max = 2) val initials: Initials, // two characters, from this constraint
    val balance: Cents,
    val referrer: EmailAddress?,
)

val member = krandom<Member> { config { seed(42L) } }
```

Value classes that wrap strings, numbers, nullable values, other value classes, or their type parameter,
alone or inside a type such as `List<T>`, all work, and so do Kotlin's unsigned types such as `UInt`. A
class whose primary constructor takes a value class is built through that constructor, unless it has a
no-argument constructor, from a secondary constructor or from defaults for every parameter: then it is
built through that one and filled field by field, as below. A `rule` or `ruleForType` for the value class gives the value class,
and a rule may give the value it wraps instead, which is boxed, as Java code must:
`rule("pin") { "1234" }` gives `Pin("1234")`. A `ruleForType<String>` reaches the `String` inside a
value class too. A value that the `init` block rejects fails generation, naming the value class and the
parameter: constrain the wrapped value with Bean Validation, as `Pin` does, or register a rule.

A value class as the generated type or as a collection element is generated with the name and the
constraints declared inside it. As the generated type, a generic value class has no type argument, so
one that wraps its type parameter needs a rule. A class whose properties are set after construction,
such as one with a no-argument constructor and `var` properties, is filled through its fields. Kotlin
stores a property of a value class type there as the value it wraps, which then gets a value under the
property's name and constraints, including a constraint on its getter, but not the value class's own
constraints or `init` block. Where the wrapped value cannot hold the property's `null`, as for a nullable
property of a value class over a primitive, Kotlin stores the value class, which is generated as above.
A generic value class is stored as an `Object` there, which fails as an unsupported type.

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
`objectOverrideDefaultInitialization` is enabled, so `ruleForType` values and generated values also
replace property initializers. A `rule(...)` replaces its property's initializer, as field rules do in
the Java API.

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

The Kotlin option uses the same core policy as Java. Object-field streams stay LEGACY unless you select INDEPENDENT. For direct configuration
consumers, `krandomConfig { seed(42L) }.snapshotClock()` captures an explicit replay clock.
