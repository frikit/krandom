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
| `scanClasspathForConcreteTypes(true)` | use explicit type/field overrides; native scanning is tracked as a parity decision |

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
