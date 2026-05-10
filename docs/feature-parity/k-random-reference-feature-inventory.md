# k-random Reference Feature Inventory

## Scope

This inventory was built from the cloned reference repository at `/private/tmp/k-random-reference`, reviewed at commit `43d5b6f4ea38b59ce73c90d9f47e3b25e9c57f32`.

The target is native feature parity in `io.github.frikit.krandom.*`, not source-compatible imports, artifact coordinates, module names, or `io.github.krandom.*` facades.

## Module Inventory

| Reference module | Reference package surface | Native krandom mapping |
| --- | --- | --- |
| `core` | `io.github.krandom`, `io.github.krandom.api`, `io.github.krandom.annotation`, `io.github.krandom.randomizers.*` | `krandom-core` with `io.github.frikit.krandom.generator.*`, especially `Generators`, `GeneratorConfig`, `ObjectGenerator`, `ObjectFaker`, object annotations, and provider namespaces. |
| `randomizers` | `io.github.krandom.randomizers.faker.*` | Native domain generators in `user`, `location`, `finance`, `network`, `identifier`, and `text` packages, usually reached through `Generators.person()`, `Generators.location()`, `Generators.finance()`, `Generators.network()`, `Generators.identifier()`, and `Generators.text()`. |
| `bean-validation` | `io.github.krandom.validation.*` | Native object-generation constraint handling in `BeanValidationSupport`; remaining constraints are tracked as parity gaps. |

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
| Setter-first population | No native setter-first mode today | Missing native feature or documented non-goal |
| `bypassSetters(true)` | Current native behavior is direct-field population | Migration note; inverse setter mode is missing |
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
| `timeRange(min, max)` | `TimeGenerator` direct use or object override for `LocalTime` until root config gains object time range | Partial |
| `objectPoolSize(size)` | `GeneratorConfig.builder().objectPoolSize(size)` | Mapped |
| `randomizationDepth(depth)` | `GeneratorConfig.builder().objectMaxDepth(depth)` | Mapped |
| `ignoreRandomizationErrors(true)` | `GeneratorConfig.builder().objectIgnoreErrors(true)` | Mapped |
| `overrideDefaultInitialization(true)` | `GeneratorConfig.builder().objectOverrideDefaultInitialization(true)` | Mapped |
| `bypassSetters(boolean)` | Direct fields are current native behavior | Partial |
| `scanClasspathForConcreteTypes(boolean)` | Explicit type/field overrides | Partial; scanning missing |
| `randomize(Class<T>, Randomizer<T>)` | `objectOverride(Class<T>, Generator<? extends T>)` | Mapped |
| `randomize(Predicate<Field>, Randomizer<T>)` | Owner/field overrides or contextual generators; arbitrary predicate randomizer registration is not one-to-one | Partial |
| `excludeField(Predicate<Field>)` | `objectExclude(Predicate<Field>)` or `objectExcludeField(name)` | Mapped |
| `excludeType(Predicate<Class<?>>)` | `objectExcludeType(Predicate<Class<?>>)` or `objectExcludeType(Class<?>)` | Mapped |
| `objectFactory(ObjectFactory)` | No public object factory hook | Missing native feature or non-goal |
| `exclusionPolicy(ExclusionPolicy)` | Exclusion predicates and annotations | Partial |
| `randomizerProvider(RandomizerProvider)` | `ProviderHub`, generator composition, and overrides | Migration-doc-only replacement |
| `randomizerRegistry(RandomizerRegistry)` | No ServiceLoader registry model | Migration-doc-only replacement unless dynamic plugin discovery becomes a goal |
| `copy()` | `GeneratorConfig.toBuilder().build()` | Mapped conceptually |

## Predicates And Annotations

| Reference feature | Native mapping | Status |
| --- | --- | --- |
| `FieldPredicates.named(regex)` | `FieldPredicates.named(exactName)` or custom predicate | Partial; regex helper missing |
| `FieldPredicates.ofType(type)` | `FieldPredicates.ofType(type)` | Covered |
| `FieldPredicates.inClass(type)` | `FieldPredicates.inClass(type)` | Covered |
| `FieldPredicates.isAnnotatedWith(varargs)` | `FieldPredicates.isAnnotatedWith(annotation)` or custom predicate | Partial; varargs helper missing |
| `FieldPredicates.hasModifiers(mask)` | `FieldPredicates.hasModifiers(mask)` | Covered |
| `TypePredicates.inPackage(prefix)` | `TypePredicates.inPackage(prefix)` | Covered |
| `TypePredicates.named(name)` | Custom predicate | Missing helper |
| `TypePredicates.ofType(type)` | `objectExcludeType(type)` for exclusions, or custom predicate | Partial |
| `TypePredicates.isAnnotatedWith(varargs)` | Custom predicate | Missing helper |
| `TypePredicates.isInterface()` | Custom predicate | Missing helper |
| `TypePredicates.isAbstract()` | Custom predicate | Missing helper |
| `TypePredicates.hasModifiers(mask)` | Custom predicate | Missing helper |
| `TypePredicates.isEnum()` | Custom predicate | Missing helper |
| `TypePredicates.isArray()` | Custom predicate | Missing helper |
| `TypePredicates.isAssignableFrom(type)` | Custom predicate | Missing helper |
| `@Exclude` | `io.github.frikit.krandom.generator.object.Exclude` | Covered for fields |
| `@Randomizer` | `io.github.frikit.krandom.generator.object.Randomizer` | Covered for fields and record components |
| `@RandomizerArgument` | `io.github.frikit.krandom.generator.object.RandomizerArgument` | Covered; constructor conversion needs parity tests |
| `@Priority` | No native registry priority annotation | Migration-doc-only replacement unless registry discovery is added |

## Extension SPI Mapping

| Reference SPI | Native replacement | Status |
| --- | --- | --- |
| `Randomizer<T>` | `Generator<T>` | Mapped |
| `ContextAwareRandomizer<T>` | `ContextualGenerator<T>` | Mapped |
| `RandomizerContext` | `GenerationContext` / contextual generator context in object generation | Partial |
| `RandomizerRegistry` | Explicit object overrides, `ProviderHub`, generator composition | Migration-doc-only replacement |
| `RandomizerProvider` | `ProviderHub` and direct generator factories | Migration-doc-only replacement |
| `ObjectFactory` | Current object instantiation strategy uses constructor then Objenesis fallback | Missing hook |
| `ExclusionPolicy` | `objectExclude`, `objectExcludeType`, `@Exclude` | Partial |
| ServiceLoader registry discovery | No native model | Intentional non-goal unless plugin-style extension is requested |

## Built-In Randomizer Families

| Reference family | Classes inventoried | Native status |
| --- | --- | --- |
| Primitive and number | `Byte`, `Short`, `Integer`, `Long`, `Float`, `Double`, `Number`, `BigInteger`, `BigDecimal`, `AtomicInteger`, `AtomicLong` randomizers and range variants | Scalar factories cover primitives and big numbers. Object generation covers atomics. `NumberRandomizer` and standalone atomic factories are migration gaps if users instantiated those classes directly. |
| Text | `Character`, `CharSequence`, `String`, `StringDelegating`, `RegularExpression` | `CharGenerator`, `StringGenerator`, and `RegexGenerator` cover core behavior. `CharSequence` maps to generated `String`. |
| Collections | `Collection`, `List`, `Set`, `Queue`, `Map`, `EnumSet`, `EnumMap` | Object fields are covered. Standalone collection randomizer classes should migrate to generator composition, `generateList`, or explicit object overrides. |
| Misc | `Boolean`, `Constant`, `Enum`, `Locale`, `Null`, `Optional`, `Skip`, `UUID` | Mostly covered by `Generators.ofBoolean`, `Generators.constant`, `EnumGenerator`, `Generators.ofLocale`, `Generators.constant(null)`, object optional handling, exclusions, and `Generators.ofUuid`. |
| Network | `UriRandomizer`, `UrlRandomizer`, `Ipv4AddressRandomizer`, `Ipv6AddressRandomizer`, `MacAddressRandomizer` | Covered by `Generators.ofUri`, `Generators.ofUrl`, `Generators.network().ipv4()`, `.ipv6()`, and `.macAddress()`. |
| Time | `Date`, `SqlDate`, `SqlTime`, `SqlTimestamp`, `Calendar`, `GregorianCalendar`, `LocalDate`, `LocalTime`, `LocalDateTime`, `Instant`, `OffsetDateTime`, `OffsetTime`, `ZonedDateTime`, `Year`, `YearMonth`, `MonthDay`, `Duration`, `JavaDuration`, `Period`, `ZoneId`, `ZoneOffset`, `TimeZone`, plus day/hour/minute/nanosecond helpers | Object generation covers the reference target Java time types. Public facade coverage is partial for standalone instantiation of `OffsetTime`, `Year`, `YearMonth`, `MonthDay`, `Period`, `ZoneId`, and `ZoneOffset`. |
| Faker/domain | `City`, `Company`, `Country`, `CreditCardNumber`, `Email`, `FirstName`, `FullName`, `GenericString`, `Ipv4Address`, `Ipv6Address`, `Isbn`, `LastName`, `Latitude`, `Longitude`, `MacAddress`, `Paragraph`, `Password`, `PhoneNumber`, `RegularExpression`, `Sentence`, `State`, `Street`, `Word`, `ZipCode` | Covered or exceeded by native person, location, finance, network, identifier, text, and base generators. Migration should use namespaces rather than old class names. |

## Bean Validation Inventory

| Reference constraint handler | Native status |
| --- | --- |
| `AssertFalse`, `AssertTrue` | Missing |
| `DecimalMin`, `DecimalMax` | Covered for `BigDecimal` when both bounds are present; single-bound and inclusive/exclusive semantics need parity checks |
| `Email` | Covered for `String` |
| `Future`, `FutureOrPresent` | Missing |
| `Max`, `Min` | Covered for `int`/`Integer` and `long`/`Long`; broader numeric types need checks |
| `Negative`, `NegativeOrZero` | Covered for `int`/`Integer` and `long`/`Long` |
| `NotBlank` | Missing |
| `Null` | Missing |
| `Past`, `PastOrPresent` | Missing |
| `Pattern` | Covered for `String` |
| `Positive`, `PositiveOrZero` | Covered for `int`/`Integer` and `long`/`Long` |
| `Size` | Covered for `String`; collection, map, array, and other `CharSequence` targets missing |
| Field annotations | Covered for supported constraints |
| Record component annotations | Covered for supported constraints |
| Getter/method annotations | Missing |

## Backlog Derived From Inventory

1. Add migration parity tests for native object generation before changing behavior.
2. Decide setter-first and classpath-scanning strategy; implement native features only if explicit overrides are insufficient.
3. Add missing Bean Validation constraints and container `@Size`.
4. Add missing TypePredicates helpers and regex/varargs FieldPredicates helpers if migration ergonomics matter.
5. Add standalone facade methods only where migration docs become awkward; prefer namespaces for domain data.
6. Keep deterministic behavior local to krandom seeds and document that exact k-random/DataFaker output strings are not promised.
