# Migrating From k-random/k-random To krandom

This guide maps k-random reference APIs to native krandom APIs. It is not a drop-in import replacement: update imports and use the `io.github.frikit.krandom.*` API surface.

## Dependency Mapping

| k-random artifact | krandom replacement | Notes |
| --- | --- | --- |
| `io.github.k-random:k-random-core` | `io.github.frikit:krandom-core` | Core generators, object generation, schema/provider APIs. |
| `io.github.k-random:k-random-randomizers` | `io.github.frikit:krandom-core` | Native generators live in provider packages instead of a randomizer facade module. |
| `io.github.k-random:k-random-bean-validation` | `io.github.frikit:krandom-core` | Bean Validation support is native to object generation where supported. |

## Basic Object Generation

k-random:

```java
KRandom random = new KRandom();
User user = random.nextObject(User.class);
List<User> users = random.objects(User.class, 10);
```

krandom:

```java
ObjectGenerator<User> users = Generators.ofObject(User.class);
User user = users.generate();
List<User> batch = users.generateList(10);
```

For stream-style bulk generation:

```java
List<User> batch = Generators.ofObject(User.class)
    .stream()
    .limit(10)
    .toList();
```

## Seeded Object Generation

k-random:

```java
KRandomParameters parameters = new KRandomParameters().seed(42L);
User user = new KRandom(parameters).nextObject(User.class);
```

krandom:

```java
GeneratorConfig config = GeneratorConfig.builder()
    .seed(42L)
    .build();

User user = Generators.ofObject(User.class, config).generate();
```

The same krandom seed and config produce repeatable krandom output. The generated values are not expected to match k-random's exact strings.

## Records And Nested Objects

Records are generated through their canonical constructor:

```java
UserRecord user = Generators.ofObject(UserRecord.class).generate();
```

Nested object fields are populated recursively until `objectMaxDepth(...)` is reached:

```java
GeneratorConfig config = GeneratorConfig.builder()
    .objectMaxDepth(3)
    .build();

Order order = Generators.ofObject(Order.class, config).generate();
```

## Arrays, Collections, Maps, And Optionals

Array, collection, map, and `Optional<T>` fields are populated from their declared types:

```java
GeneratorConfig config = GeneratorConfig.builder()
    .collectionSize(2, 5)
    .objectOptionalEmptyProbability(0.25)
    .build();

UserProfile profile = Generators.ofObject(UserProfile.class, config).generate();
```

Use `collectionSize(min, max)` to replace k-random's `collectionSizeRange(min, max)`. Use `objectOptionalEmptyProbability(...)` when you want some optional fields to become `Optional.empty()`.

## Circular References And Object Depth

krandom detects recursive object graphs and bounds traversal with an object pool and max-depth setting:

```java
GeneratorConfig config = GeneratorConfig.builder()
    .objectPoolSize(10)
    .objectMaxDepth(4)
    .build();

TreeNode node = Generators.ofObject(TreeNode.class, config).generate();
```

## Configuration Mapping

| k-random | krandom |
| --- | --- |
| `new KRandomParameters().seed(42L)` | `GeneratorConfig.builder().seed(42L)` |
| `charset(charset)` | `charset(charset)` |
| `stringLengthRange(min, max)` | `stringLength(min, max)` |
| `collectionSizeRange(min, max)` | `collectionSize(min, max)` |
| `randomizationDepth(depth)` | `objectMaxDepth(depth)` |
| `objectPoolSize(size)` | `objectPoolSize(size)` |
| `ignoreRandomizationErrors(true)` | `objectIgnoreErrors(true)` |
| `overrideDefaultInitialization(true)` | `objectOverrideDefaultInitialization(true)` |
| `dateRange(min, max)` | `objectDateRange(min, max)` where object generation is the target |
| `timeRange(min, max)` | Use `TimeGenerator` directly or an object override for `LocalTime` fields |
| `bypassSetters(true)` | krandom currently uses direct field population for object generation |
| `scanClasspathForConcreteTypes(true)` | use explicit type/field overrides for abstract/interface fields |

Example:

```java
GeneratorConfig config = GeneratorConfig.builder()
    .seed(42L)
    .stringLength(3, 16)
    .collectionSize(1, 5)
    .objectMaxDepth(3)
    .build();

User user = Generators.ofObject(User.class, config).generate();
```

Default values are not identical. k-random defaults to seed `123`, string length `1..32`, collection size `1..100`, and effectively unlimited object depth. krandom defaults to an unseeded random source, string length `5..20`, collection size `1..10`, and bounded object depth. Set the values explicitly during migration when those defaults matter.

krandom object generation writes fields directly. That is the native replacement for `bypassSetters(true)`. k-random's setter-first default is not copied as a separate mode.

For interface or abstract fields, replace `scanClasspathForConcreteTypes(true)` with an explicit override:

```java
GeneratorConfig config = GeneratorConfig.builder()
    .objectOverride(PaymentMethod.class, CardPayment::new)
    .build();
```

For `timeRange(min, max)`, prefer the dedicated time generator when generating values directly, or override `LocalTime` fields during object generation:

```java
GeneratorConfig config = GeneratorConfig.builder()
    .objectOverride(LocalTime.class, () -> LocalTime.of(9, 30))
    .build();
```

## Custom Values

k-random:

```java
KRandomParameters parameters = new KRandomParameters()
    .randomize(String.class, () -> "fixed");
```

krandom:

```java
GeneratorConfig config = GeneratorConfig.builder()
    .objectOverride(String.class, () -> "fixed")
    .build();
```

## Field Overrides

k-random field-predicate randomizers map to owner/field overrides where possible:

```java
GeneratorConfig config = GeneratorConfig.builder()
    .objectOverride(User.class, "email", () -> "user@example.com")
    .build();
```

For field-aware generation, use `ContextualGenerator`:

```java
GeneratorConfig config = GeneratorConfig.builder()
    .objectOverride(String.class, ctx -> ctx.getFieldName() + "_value")
    .build();
```

## Exclusions

k-random:

```java
new KRandomParameters().excludeField(FieldPredicates.named("password"));
```

krandom:

```java
GeneratorConfig config = GeneratorConfig.builder()
    .objectExcludeField("password")
    .build();
```

For annotations, replace k-random's `io.github.krandom.annotation.Exclude` with krandom's object-generation exclusion annotation.

## Randomizer Classes

k-random exposes randomizer classes such as `StringRandomizer`, `EmailRandomizer`, and `LocalDateRandomizer`. krandom uses generator classes and the `Generators` facade:

| k-random randomizer | krandom replacement |
| --- | --- |
| `StringRandomizer` | `Generators.ofString()` / `StringGenerator` |
| `IntegerRandomizer` | `Generators.ofInt()` / `IntGenerator` |
| `LongRandomizer` | `Generators.ofLong()` / `LongGenerator` |
| `BooleanRandomizer` | `Generators.ofBoolean()` / `BooleanGenerator` |
| `UUIDRandomizer` | `Generators.ofUuid()` / `Generators.identifier().uuid()` / `UUIDGenerator` |
| `UrlRandomizer` | `Generators.ofUrl()` / URL generator |
| `EmailRandomizer` | `Generators.ofEmail()` / `EmailGenerator` |
| `FirstNameRandomizer` | `Generators.person().firstName()` / `FirstNameGenerator` |
| `LastNameRandomizer` | `Generators.person().lastName()` / `LastNameGenerator` |
| `FullNameRandomizer` | `Generators.ofFullName()` / `Generators.person().fullName()` |
| `PasswordRandomizer` | `Generators.ofPassword()` / `Generators.person().password()` |
| `PhoneNumberRandomizer` | `Generators.ofPhoneNumber()` / `Generators.location().phoneNumber()` |
| `CityRandomizer` | `Generators.ofCity()` / `Generators.location().city()` |
| `StateRandomizer` | `Generators.ofState()` / `Generators.location().state()` |
| `CountryRandomizer` | `Generators.ofCountry()` / `Generators.location().country()` |
| `StreetRandomizer` | `Generators.ofStreetAddress()` / `Generators.location().streetAddress()` |
| `ZipCodeRandomizer` | `Generators.ofPostalCode()` / `Generators.location().postalCode()` |
| `CreditCardNumberRandomizer` | `Generators.ofCreditCard()` / `Generators.finance().creditCard()` |
| `IsbnRandomizer` | `Generators.ofIsbn()` / `Generators.identifier().isbn()` |
| `Ipv4AddressRandomizer` | `Generators.ofIPv4()` / `Generators.network().ipv4()` |
| `Ipv6AddressRandomizer` | `Generators.ofIPv6()` / `Generators.network().ipv6()` |
| `MacAddressRandomizer` | `Generators.ofMacAddress()` / `Generators.network().macAddress()` |
| `WordRandomizer` | `Generators.ofWord()` / `Generators.text().word()` |
| `SentenceRandomizer` | `Generators.ofSentence()` / `Generators.text().sentence()` |
| `ParagraphRandomizer` | `Generators.ofParagraph()` / `Generators.text().paragraph()` |
| `RegularExpressionRandomizer` | No direct `Generators` facade method; use `RegexGenerator` directly |
| `LatitudeRandomizer` / `LongitudeRandomizer` | `new CoordinatesGenerator(...).generateLatitude()` / `.generateLongitude()` |

The full mapping baseline lives in `docs/feature-parity/k-random-reference-feature-inventory.md`.

## Bean Validation

k-random loads Bean Validation support through a separate registry module. krandom handles supported constraints during native object generation. The parity plan tracks remaining gaps such as temporal constraints, null/not-blank variants, collection/map/array size, and getter-method annotations.

## Determinism

Both libraries support seeded generation, but identical seed values are not guaranteed to produce identical strings across libraries. Use krandom seeds to get deterministic krandom output after migration.

## Tracked Gaps

See `docs/plans/k-random-reference-100-feature-parity-plan.md` for the implementation order, `docs/feature-parity/k-random-reference-parity.md` for the current audit, and `docs/feature-parity/k-random-reference-feature-inventory.md` for the detailed mapping baseline.
