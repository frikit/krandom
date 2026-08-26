# Migrating From k-random/k-random To krandom

This guide maps k-random reference APIs to native krandom APIs. It is not a drop-in import replacement: update imports and use the `io.github.frikit.krandom.*` API surface.

## Dependency Mapping

| k-random artifact | krandom replacement | Notes |
| --- | --- | --- |
| `io.github.k-random:k-random-core` | `io.github.frikit:krandom-core` | Core generators, object generation, schema/provider APIs. |
| `io.github.k-random:k-random-randomizers` | `io.github.frikit:krandom-core` | Native generators live in provider packages instead of a randomizer facade module. |
| `io.github.k-random:k-random-bean-validation` | `io.github.frikit:krandom-core` | Bean Validation support is native to object generation where supported. |

Current documented krandom install coordinate:

Gradle Kotlin:

```kotlin
dependencies {
    implementation("io.github.frikit:krandom-core:2.1.0")
}
```

Gradle Groovy:

```groovy
dependencies {
    implementation 'io.github.frikit:krandom-core:2.1.0'
}
```

Maven:

```xml
<dependency>
  <groupId>io.github.frikit</groupId>
  <artifactId>krandom-core</artifactId>
  <version>2.1.0</version>
</dependency>
```

## `KRandom` Entry Point Mapping

| k-random `KRandom` usage | krandom replacement |
| --- | --- |
| `new KRandom()` | `Generators.ofObject(Type.class)` when generating objects, or a specific `Generators.of*()` factory for scalar/domain values |
| `new KRandom(parameters)` | `Generators.ofObject(Type.class, config)` with `GeneratorConfig` |
| `random.nextObject(User.class)` | `Generators.ofObject(User.class).generate()` |
| `random.objects(User.class, 10)` | `Generators.ofObject(User.class).generateList(10)` or `.stream().limit(10).toList()` |
| `random.nextInt(...)`, `nextLong(...)`, and other `Random` inherited calls | Dedicated scalar factories such as `Generators.ofInt(...)`, `Generators.ofLong(...)`, or a caller-owned `java.util.Random` |

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

k-random:

```java
KRandomParameters parameters = new KRandomParameters()
    .randomize(FieldPredicates.named("email"), () -> "user@example.com");

User user = new KRandom(parameters).nextObject(User.class);
```

krandom:

```java
GeneratorConfig config = GeneratorConfig.builder()
    .objectOverride(User.class, "email", () -> "user@example.com")
    .build();

User user = Generators.ofObject(User.class, config).generate();
```

For field-aware generation, use `ContextualGenerator`:

```java
GeneratorConfig config = GeneratorConfig.builder()
    .objectOverride(String.class, ctx -> ctx.getFieldName() + "_value")
    .build();
```

## Type Overrides

Use type overrides when a k-random type randomizer supplied a concrete value for an abstract class, interface, or common value type.

k-random:

```java
KRandomParameters parameters = new KRandomParameters()
    .randomize(PaymentMethod.class, CardPayment::new);

Checkout checkout = new KRandom(parameters).nextObject(Checkout.class);
```

krandom:

```java
GeneratorConfig config = GeneratorConfig.builder()
    .objectOverride(PaymentMethod.class, CardPayment::new)
    .build();

Checkout checkout = Generators.ofObject(Checkout.class, config).generate();
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

For annotations, replace k-random's `io.github.krandom.annotation.Exclude` with krandom's `io.github.frikit.krandom.generator.object.Exclude`.

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

Annotation mapping:

| k-random annotation | krandom replacement |
| --- | --- |
| `@io.github.krandom.annotation.Exclude` | `@io.github.frikit.krandom.generator.object.Exclude` |
| `@io.github.krandom.annotation.Randomizer` | `@io.github.frikit.krandom.generator.object.Randomizer` |
| `@io.github.krandom.annotation.RandomizerArgument` | `@io.github.frikit.krandom.generator.object.RandomizerArgument` |

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

Extension point mapping:

| k-random extension point | krandom replacement |
| --- | --- |
| `Randomizer<T>` | `Generator<T>` |
| `ContextAwareRandomizer<T>` | `ContextualGenerator<T>` |
| `RandomizationContext` / `RandomizerContext` | `GenerationContext` for contextual object-generation overrides; full root/current object path state is not copied |
| `RandomizerRegistry` | explicit `GeneratorConfig.objectOverride(...)` registrations or `ProviderHub` registrations |
| `RandomizerProvider` | `ProviderHub` or a generator factory method |
| `ObjectFactory` / `ObjenesisObjectFactory` | native constructor/Objenesis object creation plus explicit type or field overrides for special construction |
| `ExclusionPolicy` | `objectExclude(...)`, `objectExcludeType(...)`, predicate helpers, or `@Exclude` |
| `BeanValidationRandomizerRegistry` | native Bean Validation support in `krandom-core`; no registry module is required |
| ServiceLoader registry discovery / `@Priority` | explicit registration order and `ConflictPolicy`; no source-compatible ServiceLoader surface is added |

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
| `AbstractRandomizer` / `FakerBasedRandomizer` | Implement `Generator<T>` directly or wrap an existing krandom generator; no base class is required |
| `AbstractRangeRandomizer` | Use bounded generator constructors, facade methods, `between(...)` methods, or a small `Generator<T>` lambda for uncommon ranges |
| `StringRandomizer` | `Generators.ofString()` / `StringGenerator` |
| `CharacterRandomizer` | `Generators.ofChar()` / `CharGenerator` |
| `CharSequenceRandomizer` | `Generators.ofString()` / generated `String` |
| `StringDelegatingRandomizer` | Wrap or map any `Generator<String>` with `map(...)` |
| `ByteRandomizer` | `Generators.ofByte()` / `ByteGenerator` |
| `ByteRangeRandomizer` | `Generators.ofByte(min, max)` / `ByteGenerator` |
| `ShortRandomizer` | `Generators.ofShort()` / `ShortGenerator` |
| `ShortRangeRandomizer` | `Generators.ofShort(min, max)` / `ShortGenerator` |
| `IntegerRandomizer` | `Generators.ofInt()` / `IntGenerator` |
| `IntRangeRandomizer` | `Generators.ofInt(min, max)` / `IntGenerator` |
| `LongRandomizer` | `Generators.ofLong()` / `LongGenerator` |
| `LongRangeRandomizer` | `Generators.ofLong(min, max)` / `LongGenerator` |
| `FloatRandomizer` | `Generators.ofFloat()` / `FloatGenerator` |
| `FloatRangeRandomizer` | `Generators.ofFloat(min, max)` / `FloatGenerator` |
| `DoubleRandomizer` | `Generators.ofDouble()` / `DoubleGenerator` |
| `DoubleRangeRandomizer` | `Generators.ofDouble(min, max)` / `DoubleGenerator` |
| `NumberRandomizer` | `Generators.ofNumber()` / `NumberGenerator` |
| `BigIntegerRandomizer` | `Generators.ofBigInteger()` / `BigIntegerGenerator` |
| `BigIntegerRangeRandomizer` | `Generators.ofBigInteger(min, max)` / `BigIntegerGenerator` |
| `BigDecimalRandomizer` | `Generators.ofBigDecimal()` / `BigDecimalGenerator` |
| `BigDecimalRangeRandomizer` | `Generators.ofBigDecimal(min, max)` / `BigDecimalGenerator` |
| `AtomicIntegerRandomizer` | `Generators.ofAtomicInteger()` / `AtomicIntegerGenerator` |
| `AtomicLongRandomizer` | `Generators.ofAtomicLong()` / `AtomicLongGenerator` |
| `BooleanRandomizer` | `Generators.ofBoolean()` / `BooleanGenerator` |
| `ConstantRandomizer` | `Generators.ofConstant(value)` |
| `NullRandomizer` | `Generators.ofConstant(null)` |
| `SkipRandomizer` | `objectExclude(...)`, `objectExcludeField(...)`, or omit the target field |
| `EnumRandomizer` | `new EnumGenerator<>(EnumType.class)` |
| `OptionalRandomizer` | Object optional field handling or wrap a generator with `Optional.ofNullable(...)` |
| `LocaleRandomizer` | `Generators.ofLocale()` / `RandomLocaleGenerator` |
| `UUIDRandomizer` | `Generators.ofUuid()` / `Generators.identifier().uuid()` / `UUIDGenerator` (`ofUuid()` is the only UUID facade spelling) |
| `UriRandomizer` | `Generators.ofURI()` for `URI`, or `Generators.ofUri()` for URI strings (upper-case names return JDK objects; lower-case names return text) |
| `UrlRandomizer` | `Generators.ofURL()` for `URL`, or `Generators.ofUrl()` for URL strings (upper-case names return JDK objects; lower-case names return text) |
| `DateRandomizer` | `Generators.ofUtilDate()` / `UtilDateGenerator` |
| `DateRangeRandomizer` | `new UtilDateGenerator(minDate, maxDate)` after converting bounds to `LocalDate`, or `objectDateRange(...)` for object fields |
| `SqlDateRandomizer` | `Generators.ofSqlDate()` / `SqlDateGenerator` |
| `SqlDateRangeRandomizer` | `new SqlDateGenerator(minDate, maxDate)` |
| `SqlTimeRandomizer` | `Generators.ofSqlTime()` / `SqlTimeGenerator` |
| `SqlTimestampRandomizer` | `Generators.ofSqlTimestamp()` / `SqlTimestampGenerator` |
| `CalendarRandomizer` / `GregorianCalendarRandomizer` | `Generators.ofCalendar()` / `CalendarGenerator` |
| `LocalDateRandomizer` | `Generators.ofLocalDate()` / `Generators.datetime().localDate()` |
| `LocalDateRangeRandomizer` | `new DateGenerator(minDate, maxDate)` or `new DateGenerator().between(minDate, maxDate)` |
| `LocalTimeRandomizer` | `Generators.ofLocalTime()` / `Generators.datetime().localTime()` |
| `LocalTimeRangeRandomizer` | Use a custom `Generator<LocalTime>` over `toNanoOfDay()` bounds, or a field/type override for the target field |
| `LocalDateTimeRandomizer` | `Generators.ofLocalDateTime()` / `Generators.datetime().localDateTime()` |
| `LocalDateTimeRangeRandomizer` | `new LocalDateTimeGenerator().between(minDateTime, maxDateTime)` |
| `InstantRandomizer` | `Generators.ofInstant()` / `Generators.datetime().instant()` |
| `InstantRangeRandomizer` | `new InstantGenerator(minDate, maxDate)` for date-bounded instants, or a custom `Generator<Instant>` for exact instant bounds |
| `OffsetDateTimeRandomizer` | `Generators.ofOffsetDateTime()` / `Generators.datetime().offsetDateTime()` |
| `OffsetDateTimeRangeRandomizer` | `new OffsetDateTimeGenerator().between(minOffsetDateTime, maxOffsetDateTime)` |
| `OffsetTimeRandomizer` | `Generators.ofOffsetTime()` / `Generators.datetime().offsetTime()` |
| `OffsetTimeRangeRandomizer` | Use a custom `Generator<OffsetTime>` over second/nano-of-day bounds, or a field/type override for the target field |
| `ZonedDateTimeRandomizer` | `Generators.ofZonedDateTime()` / `Generators.datetime().zonedDateTime()` |
| `ZonedDateTimeRangeRandomizer` | `new ZonedDateTimeGenerator(minDate, maxDate)` for date-bounded values, or a custom `Generator<ZonedDateTime>` for exact zoned bounds |
| `YearRandomizer` | `Generators.ofYear()` / `Generators.datetime().year()` |
| `YearRangeRandomizer` | `new YearGenerator(minYear, maxYear)` |
| `YearMonthRandomizer` | `Generators.ofYearMonth()` / `Generators.datetime().yearMonth()` |
| `YearMonthRangeRandomizer` | `new YearMonthGenerator(minYear, maxYear)`; use a custom generator if exact month bounds matter |
| `MonthDayRandomizer` | `Generators.ofMonthDay()` / `Generators.datetime().monthDay()` |
| `MonthDayRangeRandomizer` | Use a custom `Generator<MonthDay>` or a field/type override for exact month-day bounds |
| `DurationRandomizer` / `JavaDurationRandomizer` | `Generators.ofDuration()` / `Generators.datetime().duration()` |
| `PeriodRandomizer` | `Generators.ofPeriod()` / `Generators.datetime().period()` |
| `ZoneIdRandomizer` | `Generators.ofZoneId()` / `Generators.datetime().zoneId()` |
| `ZoneOffsetRandomizer` | `Generators.ofZoneOffset()` / `Generators.datetime().zoneOffset()` |
| `TimeZoneRandomizer` | `Generators.ofTimeZone()` / `Generators.datetime().timeZone()` |
| `DayRandomizer` | `Generators.ofInt(1, 29)` |
| `HourRandomizer` | `Generators.ofInt(0, 24)` or `new TimeGenerator().generateHour24()` |
| `MinuteRandomizer` | `Generators.ofInt(0, 60)` or `new TimeGenerator().generateMinute()` |
| `NanoSecondRandomizer` | `Generators.ofInt(0, 1_000_000_000)` |
| `EmailRandomizer` | `Generators.ofEmail()` / `EmailGenerator` |
| `FirstNameRandomizer` | `Generators.person().firstName()` / `FirstNameGenerator` |
| `LastNameRandomizer` | `Generators.person().lastName()` / `LastNameGenerator` |
| `FullNameRandomizer` | `Generators.ofFullName()` / `Generators.person().fullName()` |
| `PasswordRandomizer` | `Generators.ofPassword()` / `Generators.person().password()` |
| `PhoneNumberRandomizer` | `Generators.ofPhoneNumber()` / `Generators.location().phoneNumber()` |
| `CityRandomizer` | `Generators.ofCity()` / `Generators.location().city()` |
| `CompanyRandomizer` | `Generators.ofCompanyName()` / `Generators.person().companyName()` |
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
| `GenericStringRandomizer` | `Generators.pick(List.of(words))` or any `Generator<String>` backed by your word list |
| `LatitudeRandomizer` / `LongitudeRandomizer` | `new CoordinatesGenerator(...).generateLatitude()` / `.generateLongitude()` |
| `CollectionRandomizer`, `ListRandomizer`, `SetRandomizer`, `QueueRandomizer` | Use `generator.generateList(size)`, `Generators.repeat(...)`, selection helpers, or object field generation |
| `MapRandomizer`, `EnumMapRandomizer`, `EnumSetRandomizer` | Use typed object fields, generator composition, or explicit object overrides |

The full mapping baseline lives in `docs/feature-parity/k-random-reference-feature-inventory.md`.

## Faker And Domain Generators

Most k-random faker/DataFaker randomizers migrate to krandom facade factories or fluent namespaces.

k-random:

```java
String firstName = new FirstNameRandomizer().getRandomValue();
String email = new EmailRandomizer().getRandomValue();
String city = new CityRandomizer().getRandomValue();
```

krandom:

```java
GeneratorConfig config = GeneratorConfig.builder()
    .seed(20260511L)
    .build();

String firstName = Generators.person(config).firstName().generate();
String email = Generators.ofEmail(config).generate();
String city = Generators.location(config).city().generate();
```

## Bean Validation

k-random loads Bean Validation support through `k-random-bean-validation`. krandom handles the equivalent behavior natively during object generation; keep using `krandom-core`.

Supported native constraints include:

| Bean Validation constraint | krandom object-generation behavior |
| --- | --- |
| `@AssertFalse`, `@AssertTrue` | Generates matching `boolean`/`Boolean` values; contradictory assertions fail before generation |
| `@Null` | Generates `null` for reference fields |
| `@NotNull` | Prevents nullable object-generation policies from returning `null` |
| `@NotEmpty` | Generates non-empty strings, arrays, collections, and maps |
| `@NotBlank` | Generates non-blank strings |
| `@Size` | Respects string, array, list, set, queue, collection, and map sizes |
| `@Min`, `@Max` | Intersects numeric bounds for primitive/wrapper numbers, `Number`, `BigInteger`, `BigDecimal`, and numeric strings; empty target domains fail before generation |
| `@DecimalMin`, `@DecimalMax` | Intersects decimal bounds exactly, including single-sided and exclusive bounds |
| `@Positive`, `@PositiveOrZero`, `@Negative`, `@NegativeOrZero` | Intersects sign rules with numeric bounds and rejects contradictions |
| `@Past`, `@PastOrPresent`, `@Future`, `@FutureOrPresent` | Intersects temporal directions for common Java time types using the configured generation clock |
| `@Pattern` | Intersects repeatable regexes with email, numeric, size, and blankness rules through a bounded search |
| `@Email` | Generates valid email text and intersects custom email regex, pattern, size, and blankness rules |

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

k-random:

```java
KRandom random = new KRandom(new KRandomParameters());
Account account = random.nextObject(Account.class);
```

krandom:

```java
Account account = Generators.ofObject(Account.class).generate();
```

## Determinism

Both libraries support seeded generation, but a seed is scoped to the library that interprets it.

krandom guarantees repeatable krandom output when these inputs stay the same:

- krandom version
- `GeneratorConfig`, including seed, locale, string length, collection size, date ranges, semantic mode, and overrides
- generator entry point and call order
- your custom generators and providers

k-random seed values do not guarantee identical krandom strings or object snapshots. In particular, k-random/DataFaker approved-output strings are not copied unless a krandom generator explicitly documents that behavior.

k-random:

```java
KRandomParameters parameters = new KRandomParameters().seed(123L);
User first = new KRandom(parameters).nextObject(User.class);
User second = new KRandom(parameters).nextObject(User.class);
```

krandom:

```java
GeneratorConfig config = GeneratorConfig.builder()
    .seed(123L)
    .stringLength(3, 16)
    .collectionSize(1, 5)
    .objectMaxDepth(3)
    .build();

User first = Generators.ofObject(User.class, config).generate();
User second = Generators.ofObject(User.class, config).generate();
```

`first` and `second` are repeatable krandom outputs for the same krandom version and config. They are not expected to equal k-random's `seed(123L)` output.

If your old tests relied on k-random defaults, set the equivalent knobs explicitly:

```java
GeneratorConfig config = GeneratorConfig.builder()
    .seed(123L)          // k-random's default seed
    .stringLength(1, 32) // k-random default string range
    .collectionSize(1, 100)
    .objectMaxDepth(10)  // choose an explicit practical depth for your model
    .build();
```

krandom's native defaults remain different: unseeded random source, string length `5..20`, collection size `1..10`, and bounded object depth.

## Tracked Gaps

See `docs/plans/k-random-reference-100-feature-parity-plan.md` for the implementation order, `docs/feature-parity/k-random-reference-parity.md` for the current audit, and `docs/feature-parity/k-random-reference-feature-inventory.md` for the detailed mapping baseline.
