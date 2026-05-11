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

Field predicate mapping:

| k-random | krandom |
| --- | --- |
| `FieldPredicates.named("pass.*")` | `FieldPredicates.nameMatches("pass.*")` |
| `FieldPredicates.ofType(String.class)` | `FieldPredicates.ofType(String.class)` |
| `FieldPredicates.inClass(User.class)` | `FieldPredicates.inClass(User.class)` |
| `FieldPredicates.isAnnotatedWith(A.class, B.class)` | `FieldPredicates.isAnnotatedWith(A.class, B.class)` |
| `FieldPredicates.hasModifiers(Modifier.PRIVATE)` | `FieldPredicates.hasModifiers(Modifier.PRIVATE)` |

Type predicate mapping:

| k-random | krandom |
| --- | --- |
| `TypePredicates.named("java.time.LocalDate")` | `TypePredicates.named("java.time.LocalDate")` |
| `TypePredicates.ofType(LocalDate.class)` | `TypePredicates.ofType(LocalDate.class)` |
| `TypePredicates.inPackage("java.time")` | `TypePredicates.inPackage("java.time")` |
| `TypePredicates.isAnnotatedWith(A.class, B.class)` | `TypePredicates.isAnnotatedWith(A.class, B.class)` |
| `TypePredicates.isInterface()` | `TypePredicates.isInterface()` |
| `TypePredicates.isAbstract()` | `TypePredicates.isAbstract()` |
| `TypePredicates.hasModifiers(Modifier.ABSTRACT)` | `TypePredicates.hasModifiers(Modifier.ABSTRACT)` |
| `TypePredicates.isEnum()` | `TypePredicates.isEnum()` |
| `TypePredicates.isArray()` | `TypePredicates.isArray()` |
| `TypePredicates.isAssignableFrom(Concrete.class)` | `TypePredicates.isAssignableFrom(Concrete.class)` |

Predicates are regular Java `Predicate` instances, so `.and(...)`, `.or(...)`, and `.negate()` work for composition.

## Declarative Randomizers

k-random:

```java
class User {
    @io.github.krandom.annotation.Randomizer(MyRandomizer.class)
    @io.github.krandom.annotation.RandomizerArgument(type = int.class, value = "7")
    private String token;
}
```

krandom:

```java
class User {
    @io.github.frikit.krandom.generator.object.Randomizer(MyGenerator.class)
    @io.github.frikit.krandom.generator.object.RandomizerArgument(type = int.class, value = "7")
    private String token;
}
```

The native `@Randomizer` expects a `Generator<?>` implementation. Constructor arguments support common primitive/wrapper values, enums, big numbers, Java/SQL date-time values, Java time values, and arrays.

## Custom Randomizers And Registries

k-random `Randomizer<T>` maps to krandom `Generator<T>`:

```java
Generator<String> tokenGenerator = () -> "fixed-token";

GeneratorConfig config = GeneratorConfig.builder()
    .objectOverride(String.class, tokenGenerator)
    .build();
```

k-random `ContextAwareRandomizer<T>` maps to `ContextualGenerator<T>`:

```java
GeneratorConfig config = GeneratorConfig.builder()
    .objectOverride(String.class, ctx -> ctx.getOwnerType().getSimpleName()
        + "_" + ctx.getFieldName()
        + "_" + ctx.getDepth())
    .build();
```

For k-random `randomize(predicate, randomizer)`, use native predicate field overrides:

```java
GeneratorConfig config = GeneratorConfig.builder()
    .objectOverride(FieldPredicates.nameMatches(".*Token"), () -> "redacted")
    .objectOverride(FieldPredicates.ofType(String.class), ctx -> ctx.getFieldName() + "_value")
    .build();
```

For registry or provider patterns, prefer explicit registration and generator composition:

```java
ProviderHub hub = new ProviderHub(GeneratorConfig.builder()
    .locale(Locale.UK)
    .build());

hub.register("tokens.session", cfg -> (Generator<String>) () ->
    "session-" + cfg.getLocale().getCountry());
hub.registerAlias("session_token", "tokens.session");

Generator<String> sessionToken = hub.get("session_token", Generator.class);

GeneratorConfig config = GeneratorConfig.builder()
    .objectOverride(User.class, "sessionToken", sessionToken)
    .build();
```

`ObjectFactory` migrations usually do not need a hook. krandom uses constructors when available and Objenesis fallback when needed. For a special construction rule, use a type or field override:

```java
GeneratorConfig config = GeneratorConfig.builder()
    .objectOverride(ExternalId.class, () -> new ExternalId("fixed-id"))
    .build();
```

`ExclusionPolicy` maps to `objectExclude(...)`, `objectExcludeType(...)`, predicate helpers, and `@Exclude`. ServiceLoader registry discovery and `@Priority` registry ordering are not copied into krandom; use explicit registration order, aliases, and `ConflictPolicy` instead.

## Randomizer Classes

k-random exposes randomizer classes such as `StringRandomizer`, `EmailRandomizer`, and `LocalDateRandomizer`. krandom uses generator classes and the `Generators` facade:

| k-random randomizer | krandom replacement |
| --- | --- |
| `StringRandomizer` | `Generators.ofString()` / `StringGenerator` |
| `CharacterRandomizer` | `Generators.ofChar()` / `CharGenerator` |
| `CharSequenceRandomizer` | `Generators.ofString()` / generated `String` |
| `StringDelegatingRandomizer` | Wrap or map any `Generator<String>` with `map(...)` |
| `ByteRandomizer` | `Generators.ofByte()` / `ByteGenerator` |
| `ShortRandomizer` | `Generators.ofShort()` / `ShortGenerator` |
| `IntegerRandomizer` | `Generators.ofInt()` / `IntGenerator` |
| `LongRandomizer` | `Generators.ofLong()` / `LongGenerator` |
| `FloatRandomizer` | `Generators.ofFloat()` / `FloatGenerator` |
| `DoubleRandomizer` | `Generators.ofDouble()` / `DoubleGenerator` |
| `NumberRandomizer` | `Generators.ofNumber()` / `NumberGenerator` |
| `BigIntegerRandomizer` | `Generators.ofBigInteger()` / `BigIntegerGenerator` |
| `BigDecimalRandomizer` | `Generators.ofBigDecimal()` / `BigDecimalGenerator` |
| `AtomicIntegerRandomizer` | `Generators.ofAtomicInteger()` / `AtomicIntegerGenerator` |
| `AtomicLongRandomizer` | `Generators.ofAtomicLong()` / `AtomicLongGenerator` |
| `BooleanRandomizer` | `Generators.ofBoolean()` / `BooleanGenerator` |
| `ConstantRandomizer` | `Generators.constant(value)` |
| `NullRandomizer` | `Generators.constant(null)` |
| `SkipRandomizer` | `objectExclude(...)`, `objectExcludeField(...)`, or omit the target field |
| `EnumRandomizer` | `new EnumGenerator<>(EnumType.class)` |
| `OptionalRandomizer` | Object optional field handling or wrap a generator with `Optional.ofNullable(...)` |
| `LocaleRandomizer` | `Generators.ofLocale()` / `RandomLocaleGenerator` |
| `UUIDRandomizer` | `Generators.ofUuid()` / `Generators.identifier().uuid()` / `UUIDGenerator` |
| `UriRandomizer` | `Generators.ofURI()` for `URI`, or `Generators.ofUri()` for URI strings |
| `UrlRandomizer` | `Generators.ofURL()` for `URL`, or `Generators.ofUrl()` for URL strings |
| `DateRandomizer` | `Generators.ofUtilDate()` / `UtilDateGenerator` |
| `SqlDateRandomizer` | `Generators.ofSqlDate()` / `SqlDateGenerator` |
| `SqlTimeRandomizer` | `Generators.ofSqlTime()` / `SqlTimeGenerator` |
| `SqlTimestampRandomizer` | `Generators.ofSqlTimestamp()` / `SqlTimestampGenerator` |
| `CalendarRandomizer` / `GregorianCalendarRandomizer` | `Generators.ofCalendar()` / `CalendarGenerator` |
| `LocalDateRandomizer` | `Generators.ofLocalDate()` / `Generators.datetime().localDate()` |
| `LocalTimeRandomizer` | `Generators.ofLocalTime()` / `Generators.datetime().localTime()` |
| `LocalDateTimeRandomizer` | `Generators.ofLocalDateTime()` / `Generators.datetime().localDateTime()` |
| `InstantRandomizer` | `Generators.ofInstant()` / `Generators.datetime().instant()` |
| `OffsetDateTimeRandomizer` | `Generators.ofOffsetDateTime()` / `Generators.datetime().offsetDateTime()` |
| `OffsetTimeRandomizer` | `Generators.ofOffsetTime()` / `Generators.datetime().offsetTime()` |
| `ZonedDateTimeRandomizer` | `Generators.ofZonedDateTime()` / `Generators.datetime().zonedDateTime()` |
| `YearRandomizer` | `Generators.ofYear()` / `Generators.datetime().year()` |
| `YearMonthRandomizer` | `Generators.ofYearMonth()` / `Generators.datetime().yearMonth()` |
| `MonthDayRandomizer` | `Generators.ofMonthDay()` / `Generators.datetime().monthDay()` |
| `DurationRandomizer` / `JavaDurationRandomizer` | `Generators.ofDuration()` / `Generators.datetime().duration()` |
| `PeriodRandomizer` | `Generators.ofPeriod()` / `Generators.datetime().period()` |
| `ZoneIdRandomizer` | `Generators.ofZoneId()` / `Generators.datetime().zoneId()` |
| `ZoneOffsetRandomizer` | `Generators.ofZoneOffset()` / `Generators.datetime().zoneOffset()` |
| `TimeZoneRandomizer` | `Generators.ofTimeZone()` / `Generators.datetime().timeZone()` |
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
| `RegularExpressionRandomizer` | `Generators.ofRegex(pattern)` / `RegexGenerator` |
| `LatitudeRandomizer` / `LongitudeRandomizer` | `new CoordinatesGenerator(...).generateLatitude()` / `.generateLongitude()` |
| `CollectionRandomizer`, `ListRandomizer`, `SetRandomizer`, `QueueRandomizer` | Use `generator.generateList(size)`, `Generators.repeat(...)`, selection helpers, or object field generation |
| `MapRandomizer`, `EnumMapRandomizer`, `EnumSetRandomizer` | Use typed object fields, generator composition, or explicit object overrides |

The full mapping baseline lives in `docs/feature-parity/k-random-reference-feature-inventory.md`.

## Bean Validation

k-random loads Bean Validation support through `k-random-bean-validation`. krandom handles the equivalent behavior natively during object generation; keep using `krandom-core`.

Supported native constraints include:

| Bean Validation constraint | krandom object-generation behavior |
| --- | --- |
| `@AssertFalse`, `@AssertTrue` | Generates matching `boolean`/`Boolean` values |
| `@Null` | Generates `null` for reference fields |
| `@NotBlank` | Generates non-blank strings |
| `@Size` | Respects string, array, list, set, queue, collection, and map sizes |
| `@Min`, `@Max` | Respects numeric bounds for primitive/wrapper numbers, `Number`, `BigInteger`, `BigDecimal`, and numeric strings |
| `@DecimalMin`, `@DecimalMax` | Respects decimal bounds, including single-sided and exclusive bounds |
| `@Positive`, `@PositiveOrZero`, `@Negative`, `@NegativeOrZero` | Generates matching signed numeric values |
| `@Past`, `@PastOrPresent`, `@Future`, `@FutureOrPresent` | Generates valid common Java temporal values |
| `@Pattern` | Generates strings matching the regexp |
| `@Email` | Generates email-like strings |

Field annotations, JavaBean getters, boolean getters, record accessors, and interface accessor declarations are recognized.

Example:

```java
class Account {
    @NotBlank
    @Size(min = 3, max = 16)
    private String username;

    @Email
    private String email;

    @Size(min = 2, max = 4)
    private List<Integer> scores;

    @Future
    private Instant expiresAt;
}

Account account = Generators.ofObject(Account.class).generate();
```

## Determinism

Both libraries support seeded generation, but identical seed values are not guaranteed to produce identical strings across libraries. Use krandom seeds to get deterministic krandom output after migration.

## Tracked Gaps

See `docs/plans/k-random-reference-100-feature-parity-plan.md` for the implementation order, `docs/feature-parity/k-random-reference-parity.md` for the current audit, and `docs/feature-parity/k-random-reference-feature-inventory.md` for the detailed mapping baseline.
