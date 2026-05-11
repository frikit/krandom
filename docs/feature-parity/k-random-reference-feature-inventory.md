# k-random Reference Feature Inventory

## Scope

This inventory was built from the cloned reference repository at `/private/tmp/k-random-reference`, reviewed at commit `43d5b6f4ea38b59ce73c90d9f47e3b25e9c57f32`.

The target is native feature parity in `io.github.frikit.krandom.*`, not source-compatible imports, artifact coordinates, module names, or `io.github.krandom.*` facades.

## Module Inventory

| Reference module | Reference package surface | Native krandom mapping |
| --- | --- | --- |
| `core` | `io.github.krandom`, `io.github.krandom.api`, `io.github.krandom.annotation`, `io.github.krandom.randomizers.*` | `krandom-core` with `io.github.frikit.krandom.generator.*`, especially `Generators`, `GeneratorConfig`, `ObjectGenerator`, `ObjectFaker`, object annotations, and provider namespaces. |
| `randomizers` | `io.github.krandom.randomizers.faker.*` | Native domain generators in `user`, `location`, `finance`, `network`, `identifier`, and `text` packages, usually reached through `Generators.person()`, `Generators.location()`, `Generators.finance()`, `Generators.network()`, `Generators.identifier()`, and `Generators.text()`. |
| `bean-validation` | `io.github.krandom.validation.*` | Native object-generation constraint handling in `BeanValidationSupport`; no compatibility module is required. |

## Entry Points And Object Generation

| Reference feature | Native equivalent | Status |
| --- | --- | --- |
| `new KRandom()` | `Generators.ofObject(Type.class)`, `new ObjectGenerator<>(Type.class)`, or scalar/domain `Generators` factories | Mapped |
| `new KRandom(parameters)` | `Generators.ofObject(Type.class, GeneratorConfig)` or direct generator constructors taking `GeneratorConfig` | Mapped |
| `nextObject(Class<T>)` | `Generators.ofObject(Type.class).generate()` | Mapped |
| `objects(Class<T>, int)` | `Generators.ofObject(Type.class).generateList(size)` | Mapped |
| `KRandom extends java.util.Random` | No direct equivalent; krandom uses per-generator `Random` state from `GeneratorConfig` | Migration-doc-only replacement |
| Record population | `ObjectGenerator` canonical-constructor record population | Covered |
| Plain POJO population | `ObjectGenerator` with constructor-first and Objenesis fallback | Covered |
| Nested object graphs | `ObjectGenerator` recursive population | Covered |
| Arrays | `FieldGeneratorResolver` array generation in object fields | Covered |
| Collections and maps | `FieldGeneratorResolver` typed collection/map population | Covered |
| Optionals | `Optional<T>` field handling and configurable empty probability | Covered |
| Circular references | `ObjectPool` in-progress detection and cached instances | Covered |
| Object pool size | `GeneratorConfig.builder().objectPoolSize(size)` | Covered; default/validation semantics differ |
| Max depth | `GeneratorConfig.builder().objectMaxDepth(depth)` | Covered; default differs from reference's unlimited depth |
| Inherited fields | `ObjectGenerator` walks superclass fields | Covered |
| Generic collection element resolution | `FieldGeneratorResolver` resolves common generic elements | Covered, but needs migration parity tests |
| Setter-first population | No native setter-first mode today | Documented non-goal for native migration |
| `bypassSetters(true)` | Current native behavior is direct-field population | Migration-doc-only replacement |
| Final-field population attempts | Local skips final fields | Product decision if strict parity requires reference-like attempts |
| Classpath scanning for concrete subtypes | No native scanning today | Missing native feature or explicit-override replacement |

## Configuration Mapping

| `KRandomParameters` feature | Native mapping | Status |
| --- | --- | --- |
| `seed(long)` | `GeneratorConfig.builder().seed(long)` | Mapped |
| `charset(Charset)` | `GeneratorConfig.builder().charset(Charset)` | Mapped |
| `stringLengthRange(min, max)` | `GeneratorConfig.builder().stringLength(min, max)` | Mapped |
| `collectionSizeRange(min, max)` | `GeneratorConfig.builder().collectionSize(min, max)` | Mapped |
| `dateRange(min, max)` | `GeneratorConfig.builder().objectDateRange(min, max)` for object generation; date generators also expose bounded constructors | Mapped with defaults caveat |
| `timeRange(min, max)` | `TimeGenerator` direct use or object override for `LocalTime` | Migration-doc-only replacement |
| `objectPoolSize(size)` | `GeneratorConfig.builder().objectPoolSize(size)` | Mapped |
| `randomizationDepth(depth)` | `GeneratorConfig.builder().objectMaxDepth(depth)` | Mapped |
| `ignoreRandomizationErrors(true)` | `GeneratorConfig.builder().objectIgnoreErrors(true)` | Mapped |
| `overrideDefaultInitialization(true)` | `GeneratorConfig.builder().objectOverrideDefaultInitialization(true)` | Mapped |
| `bypassSetters(boolean)` | Direct fields are current native behavior | Migration-doc-only replacement |
| `scanClasspathForConcreteTypes(boolean)` | Explicit type/field overrides | Migration-doc-only replacement |
| `randomize(Class<T>, Randomizer<T>)` | `objectOverride(Class<T>, Generator<? extends T>)` | Mapped |
| `randomize(Predicate<Field>, Randomizer<T>)` | `objectOverride(Predicate<Field>, Generator<T>)` or contextual predicate overrides | Covered |
| `excludeField(Predicate<Field>)` | `objectExclude(Predicate<Field>)` or `objectExcludeField(name)` | Mapped |
| `excludeType(Predicate<Class<?>>)` | `objectExcludeType(Predicate<Class<?>>)` or `objectExcludeType(Class<?>)` | Mapped |
| `objectFactory(ObjectFactory)` | Constructor/Objenesis fallback plus explicit type or field overrides | Intentional native replacement |
| `exclusionPolicy(ExclusionPolicy)` | `objectExclude`, `objectExcludeType`, predicate helpers, and annotations | Mapped |
| `randomizerProvider(RandomizerProvider)` | `ProviderHub`, direct generator composition, and overrides | Native replacement |
| `randomizerRegistry(RandomizerRegistry)` | Explicit object overrides, `ProviderHub.register(...)`, aliases, and conflict policies | Native replacement; ServiceLoader discovery is a non-goal |
| `copy()` | `GeneratorConfig.toBuilder().build()` | Mapped conceptually |

## Predicates And Annotations

| Reference feature | Native mapping | Status |
| --- | --- | --- |
| `FieldPredicates.named(regex)` | `FieldPredicates.nameMatches(regex)` | Covered |
| `FieldPredicates.ofType(type)` | `FieldPredicates.ofType(type)` | Covered |
| `FieldPredicates.inClass(type)` | `FieldPredicates.inClass(type)` | Covered |
| `FieldPredicates.isAnnotatedWith(varargs)` | `FieldPredicates.isAnnotatedWith(varargs)` | Covered |
| `FieldPredicates.hasModifiers(mask)` | `FieldPredicates.hasModifiers(mask)` | Covered |
| `TypePredicates.inPackage(prefix)` | `TypePredicates.inPackage(prefix)` | Covered |
| `TypePredicates.named(name)` | `TypePredicates.named(name)` | Covered |
| `TypePredicates.ofType(type)` | `TypePredicates.ofType(type)` | Covered |
| `TypePredicates.isAnnotatedWith(varargs)` | `TypePredicates.isAnnotatedWith(varargs)` | Covered |
| `TypePredicates.isInterface()` | `TypePredicates.isInterface()` | Covered |
| `TypePredicates.isAbstract()` | `TypePredicates.isAbstract()` | Covered |
| `TypePredicates.hasModifiers(mask)` | `TypePredicates.hasModifiers(mask)` | Covered |
| `TypePredicates.isEnum()` | `TypePredicates.isEnum()` | Covered |
| `TypePredicates.isArray()` | `TypePredicates.isArray()` | Covered |
| `TypePredicates.isAssignableFrom(type)` | `TypePredicates.isAssignableFrom(type)` | Covered |
| `@Exclude` | `io.github.frikit.krandom.generator.object.Exclude` | Covered for fields |
| `@Randomizer` | `io.github.frikit.krandom.generator.object.Randomizer` | Covered for fields and record components |
| `@RandomizerArgument` | `io.github.frikit.krandom.generator.object.RandomizerArgument` | Covered for primitive/wrapper values, enums, big numbers, Java/SQL date-time values, Java time values, and arrays |
| `@Priority` | Explicit registration order and `ConflictPolicy`; no ServiceLoader registry priority model | Intentional non-goal |

## Extension SPI Mapping

| Reference SPI | Native replacement | Status |
| --- | --- | --- |
| `Randomizer<T>` | `Generator<T>` | Mapped |
| `ContextAwareRandomizer<T>` | `ContextualGenerator<T>` | Mapped |
| `RandomizerContext` | `GenerationContext` / contextual generator context in object generation | Native subset; root/current object and full path are not copied |
| `RandomizerRegistry` | Explicit object overrides, `ProviderHub`, generator composition | Native replacement; dynamic discovery is a non-goal |
| `RandomizerProvider` | `ProviderHub` and direct generator factories | Native replacement |
| `ObjectFactory` | Current object instantiation strategy uses constructors then Objenesis fallback; special cases use explicit overrides | Intentional native replacement |
| `ExclusionPolicy` | `objectExclude`, `objectExcludeType`, `@Exclude` | Mapped |
| ServiceLoader registry discovery | No native model | Intentional non-goal unless plugin-style extension is requested |

## Built-In Randomizer Families

| Reference family | Classes inventoried | Native status |
| --- | --- | --- |
| Primitive and number | `Byte`, `Short`, `Integer`, `Long`, `Float`, `Double`, `Number`, `BigInteger`, `BigDecimal`, `AtomicInteger`, `AtomicLong` randomizers and range variants | Covered by scalar factories, `NumberGenerator`, `AtomicIntegerGenerator`, `AtomicLongGenerator`, big-number generators, object field resolution, and `Generators.forType(...)` for standalone Java type lookup. |
| Text | `Character`, `CharSequence`, `String`, `StringDelegating`, `RegularExpression` | Covered by `CharGenerator`, `StringGenerator`, `RegexGenerator`, `Generators.ofRegex(...)`, and text namespace methods. `CharSequence` maps to generated `String`. |
| Collections | `Collection`, `List`, `Set`, `Queue`, `Map`, `EnumSet`, `EnumMap` | Covered for object fields. Standalone collection randomizer classes migrate to generator composition, `generateList`, `repeat`, `pick`, `shuffle`, or explicit object overrides. |
| Misc | `Boolean`, `Constant`, `Enum`, `Locale`, `Null`, `Optional`, `Skip`, `UUID` | Covered by `Generators.ofBoolean`, `Generators.constant`, `EnumGenerator`, `Generators.ofLocale`, `Generators.constant(null)`, object optional handling, exclusions, and `Generators.ofUuid`. |
| Network | `UriRandomizer`, `UrlRandomizer`, `Ipv4AddressRandomizer`, `Ipv6AddressRandomizer`, `MacAddressRandomizer` | Covered by typed `Generators.ofURI()`/`ofURL()` when Java objects are needed, string `Generators.ofUri()`/`ofUrl()`, and `Generators.network().ipv4()`, `.ipv6()`, and `.macAddress()`. |
| Time | `Date`, `SqlDate`, `SqlTime`, `SqlTimestamp`, `Calendar`, `GregorianCalendar`, `LocalDate`, `LocalTime`, `LocalDateTime`, `Instant`, `OffsetDateTime`, `OffsetTime`, `ZonedDateTime`, `Year`, `YearMonth`, `MonthDay`, `Duration`, `JavaDuration`, `Period`, `ZoneId`, `ZoneOffset`, `TimeZone`, plus day/hour/minute/nanosecond helpers | Covered by date/time generators, `Generators.datetime()`, `Generators.forType(...)`, and object field resolution. `LegacyTimeZoneGenerator` covers `java.util.TimeZone`; `TimezoneGenerator` remains the string timezone-id generator. |
| Faker/domain | `City`, `Company`, `Country`, `CreditCardNumber`, `Email`, `FirstName`, `FullName`, `GenericString`, `Ipv4Address`, `Ipv6Address`, `Isbn`, `LastName`, `Latitude`, `Longitude`, `MacAddress`, `Paragraph`, `Password`, `PhoneNumber`, `RegularExpression`, `Sentence`, `State`, `Street`, `Word`, `ZipCode` | Covered or exceeded by native person, location, finance, network, identifier, text, and base generators. Migration should use namespaces rather than old class names. |

## Bean Validation Inventory

| Reference constraint handler | Native status |
| --- | --- |
| `AssertFalse`, `AssertTrue` | Covered for `boolean`/`Boolean` |
| `DecimalMin`, `DecimalMax` | Covered for `BigDecimal`, `BigInteger`, primitive/wrapper numbers, `Number`, and numeric strings, including single-bound and inclusive/exclusive handling |
| `Email` | Covered for `String` |
| `Future`, `FutureOrPresent` | Covered for common Java temporal field types |
| `Max`, `Min` | Covered for primitive/wrapper numbers, `Number`, `BigInteger`, `BigDecimal`, and numeric strings |
| `Negative`, `NegativeOrZero` | Covered for primitive/wrapper numbers, `Number`, `BigInteger`, and `BigDecimal` |
| `NotBlank` | Covered for `String` |
| `Null` | Covered for reference fields |
| `Past`, `PastOrPresent` | Covered for common Java temporal field types |
| `Pattern` | Covered for `String` |
| `Positive`, `PositiveOrZero` | Covered for primitive/wrapper numbers, `Number`, `BigInteger`, and `BigDecimal` |
| `Size` | Covered for `String`, arrays, lists, sets, queues, collections, and maps |
| Field annotations | Covered |
| Record component annotations | Covered through record accessors and backing fields |
| Getter/method annotations | Covered for JavaBean getters, boolean getters, record accessors, and interface accessor declarations |

## Backlog Derived From Inventory

1. Add migration parity tests for native object generation before changing behavior. Completed across the phase-specific parity tests.
2. Add final migration examples that compile against native krandom APIs. Completed in `KRandomReferenceMigrationGuideExamplesTest`.
3. Link the migration guide from the README, docs site, and docs index. Completed.
4. Add missing TypePredicates helpers and regex/varargs FieldPredicates helpers if migration ergonomics matter.
5. Add standalone facade methods only where migration docs become awkward; prefer namespaces for domain data.
6. Keep deterministic behavior local to krandom seeds; exact k-random/DataFaker output strings remain a documented non-goal.
