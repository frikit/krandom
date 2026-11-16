---
layout: page
title: Migrating to 4.0
permalink: /guides/migrating-to-4.0/
---

# Migrating to 4.0

4.0.0 is the major release after 3.2.0, the last 3.x minor release. It removes three deprecated schema
methods, changes the types of `Schema` and `pick`, turns on four seeding settings that change seeded
output, and writes recipes as `krandom-recipe/v2`. This page lists each change from 4.0.0 and how to keep
the 3.2 values where you need them. The repository has the same guide at
[docs/migration/3.x-to-4.0.md](https://github.com/frikit/krandom/blob/main/docs/migration/3.x-to-4.0.md),
and the [changelog](https://github.com/frikit/krandom/blob/main/CHANGELOG.md) has the full entries.

## Upgrade checklist

1. **Recompile** every module that uses kRandom against 4.0.0. Code compiled against 3.x that calls a
   removed method fails with `NoSuchMethodError`, and code that reads `GenerationRecipe.RECIPE_VERSION`
   has the 3.x value `"v1"` compiled into it.
2. **Replace the removed methods:** `Schema.generateBatch(int)` and `Schema.loop(int)` with
   `nextRecords(int)`, and `SchemaProjection.project(T)` with `projectRow(T)`.
3. **Retype schema records** to `Map<String, @Nullable Object>` if you check nullness with NullAway in
   JSpecify mode, or to `Map<String, Any?>` in Kotlin.
4. **Kotlin `pick`:** check each `krandom.pick(listOf(...))` whose list holds `null`, and each value read
   from a `PickGenerator<*>`.
5. **Tests that assert exact seeded values:** update the expected values, or set the four seeding settings
   to `false` to keep the 3.2 values.
6. **Saved recipes:** a `krandom-recipe/v1` recipe from 3.x no longer replays. Replay it with the 3.x
   release it names, or record a new recipe with 4.0.0.

## Removed schema record methods

`Schema.generateBatch(int)`, `Schema.loop(int)` and `SchemaProjection.project(T)`, deprecated in 3.1.0, are
removed from 4.0.0. Their replacements return the same records and rows from the same record sequence,
typed `Map<String, @Nullable Object>`:

| 3.x | From 4.0.0 |
|---|---|
| `schema.generateBatch(count)` | `schema.nextRecords(count)` |
| `schema.loop(count)` | `schema.nextRecords(count)` |
| `projection.project(value)` | `projection.projectRow(value)` |

```java
// 3.x
List<Map<String, Object>> batch = schema.generateBatch(10);
Map<String, Object> row = projection.project(profile);

// From 4.0.0
List<Map<String, @Nullable Object>> batch = schema.nextRecords(10);
Map<String, @Nullable Object> row = projection.projectRow(profile);
```

A jar compiled against 3.x that calls a removed method fails at run time with `NoSuchMethodError`, so
recompile it rather than only change the dependency version. See
[Schema and Provider Hub]({{ '/guides/schema-and-provider-hub/' | relative_url }}) for the record methods.

## `Schema` is a generator of nullable records

From 4.0.0 a `Schema` is a `Generator<Map<String, @Nullable Object>>`, where 3.x had
`Generator<Map<String, Object>>`, and `generate()` is no longer deprecated: it returns the record that
`nextRecord()` returns. The records, the seeded values and the JVM signatures are unchanged.

**Java.** Plain javac ignores the `@Nullable` and still accepts `Map<String, Object>`. NullAway in JSpecify
mode rejects it from 4.0.0, so declare the new type argument:

```java
// 3.x, and still accepted by plain javac
Map<String, Object> record = schema.generate();
Generator<Map<String, Object>> records = schema;

// From 4.0.0, with NullAway
Map<String, @Nullable Object> record = schema.generate();
Generator<Map<String, @Nullable Object>> records = schema;
```

**Kotlin** reads a record as `Map<String, Any?>`, so the 3.x lines no longer compile:

```kotlin
// 3.x
val name: Any = schema.generate().getValue("name")
val records: Generator<Map<String, Any>> = schema

// From 4.0.0
val name: Any = checkNotNull(schema.generate()["name"])
val nickname: Any? = schema.generate()["nickname"]
val records: Generator<Map<String, Any?>> = schema
```

## `pick` accepts a list that holds `null`

From 4.0.0 `Krandom.pick(List)`, both `Generators.pick` methods and `PickGenerator<T>` bound `T` by
`@Nullable Object`, so a list that holds `null` can be picked from, and the `null` is returned. Seeded
picks are unchanged.

`PickGenerator` keeps a `null` element, where 3.x threw a `NullPointerException`:

```java
PickGenerator<@Nullable String> picks = Generators.pick(Arrays.asList("a", null));
// 3.x: NullPointerException. From 4.0.0: "a" or null.
@Nullable String value = picks.generate();
```

In Kotlin, `krandom.pick(listOf("a", null))` now calls `pick(List)` and returns an element, where 3.x
called the varargs `pick` and returned the list itself:

```kotlin
// 3.x
val whole: List<String?> = krandom.pick(listOf("a", null))

// From 4.0.0
val one: String? = krandom.pick(listOf("a", null))
val whole: List<String?> = krandom.pick<List<String?>>(listOf("a", null))
```

Kotlin also reads a value from a `PickGenerator<*>` as `Any?` from 4.0.0:

```kotlin
val generator: PickGenerator<*> = Generators.pick(listOf("a", "b"))
val value: Any? = generator.generate() // 3.x: Any
```

The Kotest `krandomPickArb` and `Generators.pickSet`/`PickSetGenerator` are unchanged and stay non-null.

## Four seeding settings are on by default

From 4.0.0 four `GeneratorConfig` settings that 3.2.0 added as opt-ins are on by default:

| Setting | What changes from 4.0.0 |
|---|---|
| `advancingConvenienceStreams` | `EmailGenerator.generateCompanyEmail()`, the `FullNameGenerator` methods with a middle name or initial, and `CurrencyGenerator.generatePriceTag` give the next value on each call, where 3.x repeated one value. |
| `strictMathGaussian` | `NormalDistributionGenerator`, including `Generators.ofNormal()` and the constructors without a configuration, draws with `StrictMath`, so a seeded value can differ in its last bit from what `Math` gave, and is the same on every platform. |
| `objectReseedBuiltInRandomizers` | Object generation reseeds the 18 built-in `@Randomizer` generators that are not `Seedable`, such as `UUIDGenerator` and `FirstNameGenerator`, so their field values replay from the seed. |
| `germanStreetLayout` | German-language locales write a street address street first, in the full address, `AddressInfo`, `Krandom.streetAddress()`, the address schema columns, object street fields, and composites such as `PersonInfo`, `OrderInfo` and `InvoiceInfo`. See [German street layout]({{ '/guides/locale-aware-data/' | relative_url }}#german-street-layout). |

For example, with seed 42:

```java
EmailGenerator emails = new EmailGenerator(
        GeneratorConfig.builder().seed(42L).locale(Locale.US).build());
emails.generateCompanyEmail(); // aharris@advancedassociatesco.test in 3.x and from 4.0.0
emails.generateCompanyEmail(); // 3.x: aharris@advancedassociatesco.test again
                               // from 4.0.0: juliagarcia@advancedresourcesltd.test

StreetAddressGenerator streets = new StreetAddressGenerator(
        GeneratorConfig.builder().seed(42L).locale(Locale.GERMANY).build());
streets.generate(); // 3.x: 7389 Schloss Anger; from 4.0.0: Schlossanger 7389
```

### Keeping the 3.2 values

Set a setting to `false` to keep its 3.x values. With all four `false`, a seed gives what 3.2.0 gives.

```java
GeneratorConfig config = GeneratorConfig.builder()
        .seed(42L)
        .advancingConvenienceStreams(false)
        .strictMathGaussian(false)
        .objectReseedBuiltInRandomizers(false)
        .germanStreetLayout(false)
        .build();
```

In the [Kotlin DSL]({{ '/guides/kotlin-dsl/' | relative_url }}), `krandomConfig { ... }` and `config { ... }`
have the same functions:

```kotlin
val config =
    krandomConfig {
        seed(42L)
        advancingConvenienceStreams(false)
        strictMathGaussian(false)
        objectReseedBuiltInRandomizers(false)
        germanStreetLayout(false)
    }
```

The [Spring Boot starter]({{ '/guides/spring-boot-starter/' | relative_url }}) has a property for each from
4.0.0; leave one unset to keep the default:

```properties
krandom.advancing-convenience-streams=false
krandom.strict-math-gaussian=false
krandom.object-reseed-built-in-randomizers=false
krandom.german-street-layout=false
```

A recipe records all four, so each property fails startup next to `krandom.recipe`.

## Recipes are `krandom-recipe/v2`

From 4.0.0 a recipe is `krandom-recipe/v2`. It records the four settings, such as
`setting.gaussian.strict-math=false`, and replay requires all four. Every 3.x release writes
`krandom-recipe/v1`, and 4.0.0 rejects it with an `IllegalArgumentException` that names the release that
wrote it:

```text
Unsupported recipe version: v1. kRandom 4.x replays only krandom-recipe/v2 recipes: a krandom-recipe/v1 recipe was written by kRandom 3.x or earlier, and its seeded values differ from 4.0.0. This recipe records library-version 3.2.0: replay it with kRandom 3.2.0.
```

To replay a 3.x recipe, run it with the 3.x release it names, for example with
`io.github.frikit:krandom-bom:3.2.0`. To move a test to 4.0.0, keep its seed and record a new recipe.

The [JUnit extension]({{ '/guides/junit-extension/' | relative_url }}) reports a v1 recipe in
`krandom.junit.recipe` or `krandom.junit.class-recipe` as a configuration error,
`Invalid krandom.junit.recipe replay recipe: Unsupported recipe version: v1. ...`, and the Spring Boot
starter fails at startup with `Invalid krandom.recipe: Unsupported recipe version: v1. ...`.

`GenerationRecipe.RECIPE_VERSION` is a compile-time constant, `"v2"` from 4.0.0. Code compiled against 3.x
holds `"v1"`, so passing it to `GenerationRecipe.builder().recipeVersion(...)` fails at `build()`, and
comparing it with `getRecipeVersion()` no longer matches. The builder's `library-version` is the running
release unless the code sets one, and from 4.0.0 the message names it as the release to replay with only
when it is 3.x or earlier. For any other value, such as the default, it says that a library-version of 4 or
later cannot have written a v1 recipe, so replay it with the 3.x release that wrote it. Recompile such code.

## What does not change

- Java 17 is still the minimum for every module.
- `krandom-kotlin-dsl` and `krandom-kotest-extensions` are still compiled for Kotlin language and API
  version 2.2.
- `krandom-spring-boot-starter` still needs Spring Boot 4.x.
- Only the latest release is maintained, and fixes are not backported to earlier ones.

See [Primitives and Determinism]({{ '/guides/primitives-and-determinism/' | relative_url }}) for the seeding
settings and recipes, and the
[versioning policy](https://github.com/frikit/krandom/blob/main/VERSIONING.md) for the platform requirements.
