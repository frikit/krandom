# Easy Random Feature Parity Analysis

## Library Overview

- **Name**: Easy Random
- **Language**: Java
- **Version Analyzed**: 5.0.x (Java 11+), 4.3.x (Java 8)
- **GitHub**: https://github.com/j-easy/easy-random
- **License**: MIT
- **Status**: Maintenance mode (since November 2020) — bug fixes only
- **Key Strength**: Reflection-based object graph population, minimal configuration, ObjectMother pattern implementation

*Last Updated: 2026-02-25*

## Executive Summary

Easy Random is a specialized library focused on **object graph randomization** rather than realistic data generation. Unlike DataFaker (200+ providers for realistic data), Easy Random excels at *
*populating arbitrary Java objects with random values** to eliminate hand-crafted test fixtures. It implements the **ObjectMother pattern** for the JVM, making it ideal for:

- Generating test fixtures without manual builders
- Populating databases with domain objects at scale
- Testing algorithms where specific values don't matter (sorting, serialization, persistence)
- Load testing REST services with random POJOs

**Key Differentiators**:

1. **Deep object graph traversal** - recursively populates nested objects up to configurable depth
2. **Reflection-based field population** - works with any POJO via introspection
3. **Objenesis integration** - instantiates objects without no-arg constructors
4. **Bean Validation support** - generates values satisfying `@NotNull`, `@Size`, `@Min`, `@Max`, etc.
5. **Flexible exclusion policies** - field predicates, type predicates, annotations
6. **Extensible randomizer registry** - custom generators per field or type
7. **Circular reference handling** - object pool prevents infinite recursion
8. **Java Records support** - randomizes component parameters
9. **Classpath scanning** - auto-discovers concrete implementations for interfaces/abstract types

---

## Implementation Status

### Completed Features ✅

**Numbers & Statistical Generators (8 features)**

| Feature                 | krandom Implementation                           | Test Coverage | Status |
|-------------------------|--------------------------------------------------|---------------|--------|
| Natural numbers (≥0)    | `NaturalNumberGenerator`                         | 99.7%         | ✓ DONE |
| Prime number generation | `PrimeGenerator` (Sieve of Eratosthenes)         | 99.7%         | ✓ DONE |
| Fixed decimal precision | `DoubleGenerator.withPrecision(decimals)`        | 99.7%         | ✓ DONE |
| Fixed decimal precision | `FloatGenerator.withPrecision(decimals)`         | 99.7%         | ✓ DONE |
| Normal distribution     | `NormalDistributionGenerator` (Box-Muller)       | 99.7%         | ✓ DONE |
| Value exclusion         | `NaturalNumberGenerator.excluding(...)`          | 99.7%         | ✓ DONE |
| Range-based generation  | All bounded generators (Int, Long, Double, etc.) | 99.7%         | ✓ DONE |
| Deterministic seeding   | All generators support seed parameter            | 99.7%         | ✓ DONE |

**Primitives & Basic Types (9 features)**

| Feature            | krandom Implementation | Status |
|--------------------|------------------------|--------|
| boolean / Boolean  | `BooleanGenerator`     | ✓ DONE |
| byte / Byte        | `ByteGenerator`        | ✓ DONE |
| short / Short      | `ShortGenerator`       | ✓ DONE |
| int / Integer      | `IntGenerator`         | ✓ DONE |
| long / Long        | `LongGenerator`        | ✓ DONE |
| float / Float      | `FloatGenerator`       | ✓ DONE |
| double / Double    | `DoubleGenerator`      | ✓ DONE |
| char / Character   | `CharGenerator`        | ✓ DONE |
| Enum randomization | `EnumGenerator<T>`     | ✓ DONE |

**Realistic & Finance Generators (implemented)**

| Feature              | krandom Implementation                                         | Status |
|----------------------|----------------------------------------------------------------|--------|
| First name           | `FirstNameGenerator` (10 locales, gender-aware)               | ✓ DONE |
| Last name            | `LastNameGenerator` (10 locales)                              | ✓ DONE |
| Email                | `EmailGenerator` (7 formats, locale-aware)                    | ✓ DONE |
| Phone number         | `PhoneNumberGenerator` (10 locales, mobile support)           | ✓ DONE |
| City                 | `CityGenerator` (10 locales)                                  | ✓ DONE |
| State / Province     | `StateGenerator` (10 locales, full + abbrev)                  | ✓ DONE |
| Country              | `CountryGenerator` (10 locales)                               | ✓ DONE |
| ZIP / Postal code    | `PostalCodeGenerator` (10 locales)                            | ✓ DONE |
| Latitude             | `CoordinateGenerator.latitude()`                              | ✓ DONE |
| Longitude            | `CoordinateGenerator.longitude()`                             | ✓ DONE |
| Credit card          | `CreditCardGenerator` (Visa/MC/Amex/Discover, Luhn-valid)     | ✓ DONE |
| UUID v4              | `UUIDGenerator`                                               | ✓ DONE |
| URL                  | `UrlGenerator` (http/https, path, query)                      | ✓ DONE |
| Money / currency     | `MoneyGenerator` (10 locales, locale-aware formatting)        | ✓ DONE |
| Currency pair        | `CurrencyPairGenerator` (44 currencies, locale base)          | ✓ DONE |
| National ID          | `NationalIdGenerator` (10 countries: SSN, NI, TFN, NIR, etc.) | ✓ DONE |
| Birthday             | `BirthdayGenerator` (age ranges, locale-aware string output)  | ✓ DONE |
| Color (hex)          | `HexColorGenerator`                                           | ✓ DONE |
| IPv4 address         | `IPv4Generator` (RFC-compliant)                               | ✓ DONE |
| IPv6 address         | `IPv6Generator` (RFC-compliant)                               | ✓ DONE |

**Implementation Metrics:**

- **Total Features Implemented**: 37+ features (17 numeric/primitive + 20 realistic/finance)
- **Test Coverage**: 99.2%+ line, 99.1%+ branch
- **New Tests Added**: ~600 comprehensive test cases
- **Algorithms**: Sieve of Eratosthenes (primes), Box-Muller (normal dist.), Luhn (cards), ISO 7064 (national IDs)
- **Pre-commit Checks**: ALL PASSING ✅

### Planned Features ⏳

**Phase 1: Statistical & Advanced Numbers** (Next Priority)

- BigDecimal / BigInteger generators (arbitrary precision)
- Regex-based string generation

**Phase 2: Date/Time Generators**

- LocalDate, LocalTime, LocalDateTime (JSR 310)
- Range-based date/time randomizers
- Instant, ZonedDateTime, OffsetDateTime

**Phase 3: Object Generation Enhancements**

- Circular reference handling (object pool)
- Array/collection auto-population
- Field exclusion predicates

---

## Feature Categories

### 1. OBJECT GENERATION (Core Capability)

| Feature                     | Easy Random Support                      | krandom Status | Implementation Priority | Notes                           |
|-----------------------------|------------------------------------------|----------------|-------------------------|---------------------------------|
| **Basic Object Creation**   |
| Generate random POJO        | ✅ `nextObject(Class<T>)`                 | ✅ Yes          | ✓ DONE                  | krandom has ObjectGenerator<T>  |
| Generate object stream      | ✅ `objects(Class<T>, int)`               | ✅ Partial      | MEDIUM                  | krandom uses Generator.stream() |
| Extend java.util.Random     | ✅ Yes                                    | ❌ No           | LOW                     | EasyRandom extends Random       |
| Deterministic seed          | ✅ Constructor/config                     | ✅ Yes          | ✓ DONE                  | Both support seeded generation  |
| **Object Instantiation**    |
| No-arg constructor          | ✅ Yes                                    | ✅ Yes          | ✓ DONE                  | Both require no-arg constructor |
| Objenesis fallback          | ✅ Yes                                    | ❌ No           | MEDIUM                  | Bypass constructor entirely     |
| Java Records                | ✅ Canonical constructor                  | ✅ Yes          | ✓ DONE                  | Both support records            |
| Abstract/interface types    | ✅ With classpath scanning                | ❌ No           | LOW                     | krandom returns null            |
| Custom ObjectFactory        | ✅ `objectFactory(factory)`               | ❌ No           | MEDIUM                  | Pluggable instantiation         |
| **Field Population**        |
| Declared fields             | ✅ Yes                                    | ✅ Yes          | ✓ DONE                  | Instance fields only            |
| Inherited fields            | ✅ Full hierarchy                         | ✅ Yes          | ✓ DONE                  | Both walk class hierarchy       |
| Static fields               | ✅ Skipped                                | ✅ Skipped      | ✓ DONE                  | Both skip static                |
| Final fields                | ✅ Yes (reflection)                       | ✅ Yes          | ✓ DONE                  |                                 |
| Transient fields            | ✅ Yes                                    | ✅ Yes          | ✓ DONE                  | Populated by default            |
| Override existing values    | ✅ `overrideDefaultInitialization(true)`  | ❌ No           | LOW                     | Re-randomize initialized fields |
| Bypass setters              | ✅ `bypassSetters(true)`                  | ✅ Yes          | ✓ DONE                  | Direct field access             |
| **Nested Objects**          |
| Recursive population        | ✅ Yes                                    | ✅ Yes          | ✓ DONE                  | Both support                    |
| Max depth control           | ✅ `randomizationDepth(int)`              | ✅ Yes          | ✓ DONE                  | krandom: maxDepth               |
| Circular reference handling | ✅ Object pool                            | ❌ No           | HIGH                    | Prevent infinite recursion      |
| Object pool size            | ✅ `objectPoolSize(int)`                  | ❌ No           | MEDIUM                  | Cache instances per type        |
| **Generics Support**        |
| Simple generics             | ✅ `List<String>`                         | ✅ Partial      | MEDIUM                  | krandom limited support         |
| Nested generics             | ⚠️ Limited `List<List<T>>`               | ❌ No           | LOW                     | Type erasure issues             |
| Generic inheritance         | ✅ `StringList extends ArrayList<String>` | ❌ No           | LOW                     |                                 |

### 2. EXCLUSION & FILTERING

| Feature                   | Easy Random Support                          | krandom Status | Implementation Priority | Notes                   |
|---------------------------|----------------------------------------------|----------------|-------------------------|-------------------------|
| **Field Exclusion**       |
| Exclude by annotation     | ✅ `@Exclude`                                 | ❌ No           | HIGH                    | Declarative exclusion   |
| Exclude by name           | ✅ `FieldPredicates.named("password")`        | ❌ No           | HIGH                    | Regex-based matching    |
| Exclude by type           | ✅ `FieldPredicates.ofType(Class)`            | ❌ No           | HIGH                    | Type-based filtering    |
| Exclude by class          | ✅ `FieldPredicates.inClass(Class)`           | ❌ No           | MEDIUM                  | Scope to specific class |
| Exclude by annotation     | ✅ `FieldPredicates.isAnnotatedWith()`        | ❌ No           | MEDIUM                  | Match annotated fields  |
| Exclude by modifiers      | ✅ `FieldPredicates.hasModifiers(int)`        | ❌ No           | LOW                     | Access level filtering  |
| Exclude entire types      | ✅ `TypePredicates.inPackage("com.internal")` | ❌ No           | MEDIUM                  | Skip packages           |
| Custom ExclusionPolicy    | ✅ `exclusionPolicy(policy)`                  | ❌ No           | MEDIUM                  | Pluggable strategy      |
| **Predicate Composition** |
| AND logic                 | ✅ `predicate1.and(predicate2)`               | ❌ No           | MEDIUM                  | Combine predicates      |
| OR logic                  | ✅ `predicate1.or(predicate2)`                | ❌ No           | MEDIUM                  |                         |
| NOT logic                 | ✅ `predicate.negate()`                       | ❌ No           | MEDIUM                  |                         |

### 3. CUSTOM RANDOMIZERS

| Feature                | Easy Random Support                                   | krandom Status | Implementation Priority | Notes                     |
|------------------------|-------------------------------------------------------|----------------|-------------------------|---------------------------|
| **Randomizer API**     |
| Functional interface   | ✅ `Randomizer<T>`                                     | ✅ Yes          | ✓ DONE                  | krandom: Generator<T>     |
| Lambda support         | ✅ `() -> "value"`                                     | ✅ Yes          | ✓ DONE                  | Both functional           |
| Context-aware          | ✅ `ContextAwareRandomizer<T>`                         | ❌ No           | MEDIUM                  | Access runtime context    |
| RandomizerContext      | ✅ Target type, root object, current field path, depth | ❌ No           | MEDIUM                  | Contextual generation     |
| **Registration**       |
| Type-level randomizer  | ✅ `randomize(String.class, randomizer)`               | ✅ Yes          | ✓ DONE                  | krandom: typeOverrides    |
| Field-level randomizer | ✅ `randomize(predicate, randomizer)`                  | ✅ Yes          | ✓ DONE                  | krandom: fieldOverrides   |
| Annotation-based       | ✅ `@Randomizer(EmailRandomizer.class)`                | ❌ No           | HIGH                    | Declarative configuration |
| Randomizer arguments   | ✅ `@RandomizerArgument`                               | ❌ No           | MEDIUM                  | Pass constructor params   |
| **Registry System**    |
| RandomizerRegistry     | ✅ Interface + SPI discovery                           | ❌ No           | MEDIUM                  | Group randomizers         |
| Registry priority      | ✅ `@Priority` annotation                              | ❌ No           | MEDIUM                  | Override order            |
| Built-in registries    | ✅ 6 registries (Internal, Time, BeanValidation, etc.) | ❌ No           | MEDIUM                  | Layered resolution        |
| Custom registries      | ✅ ServiceLoader auto-discovery                        | ❌ No           | LOW                     | SPI extension             |
| RandomizerProvider     | ✅ Custom provider strategy                            | ❌ No           | LOW                     | Resolution algorithm      |

### 4. BEAN VALIDATION INTEGRATION

| Feature                    | Easy Random Support                 | krandom Status | Implementation Priority | Notes                           |
|----------------------------|-------------------------------------|----------------|-------------------------|---------------------------------|
| **JSR 380 Constraints**    |
| @NotNull                   | ✅ Never null                        | ❌ No           | HIGH                    | Guaranteed non-null             |
| @NotEmpty                  | ✅ Never empty string/collection     | ❌ No           | HIGH                    |                                 |
| @Size(min, max)            | ✅ Respected for strings/collections | ❌ No           | HIGH                    | Range constraints               |
| @Min / @Max                | ✅ Numeric bounds                    | ❌ No           | HIGH                    |                                 |
| @Past / @Future            | ✅ Date constraints                  | ❌ No           | MEDIUM                  | Temporal constraints            |
| @Positive / @Negative      | ✅ Sign constraints                  | ❌ No           | MEDIUM                  |                                 |
| @Email                     | ✅ Valid email format                | ❌ No           | MEDIUM                  |                                 |
| @Pattern                   | ✅ Regex-based generation            | ❌ No           | MEDIUM                  |                                 |
| **Constraint Priority**    |
| Override global config     | ✅ Yes                               | ❌ No           | MEDIUM                  | Annotation wins over parameters |
| Custom randomizer override | ✅ Yes                               | ❌ No           | MEDIUM                  | Custom > validation > global    |
| **Module**                 |
| Separate module            | ✅ `easy-random-bean-validation`     | ❌ No           | MEDIUM                  | Optional dependency             |
| BeanValidation registry    | ✅ Priority -2                       | ❌ No           | MEDIUM                  |                                 |

### 5. CONFIGURATION PARAMETERS

| Feature               | Easy Random Support                            | krandom Status | Implementation Priority | Notes                                         |
|-----------------------|------------------------------------------------|----------------|-------------------------|-----------------------------------------------|
| **Global Settings**   |
| Seed                  | ✅ `seed(long)` default: 123L                   | ✅ Yes          | ✓ DONE                  | krandom: GeneratorConfig.seed()               |
| Charset               | ✅ `charset(Charset)` default: US_ASCII         | ✅ Yes          | ✓ DONE                  | krandom: GeneratorConfig.charset()            |
| String length range   | ✅ `stringLengthRange(min, max)` [1, 32]        | ✅ Yes          | ✓ DONE                  | krandom: GeneratorConfig.stringLength()       |
| Collection size range | ✅ `collectionSizeRange(min, max)` [1, 100]     | ✅ Yes          | ✓ DONE                  | krandom: GeneratorConfig.collectionSize()     |
| Randomization depth   | ✅ `randomizationDepth(int)` default: MAX_VALUE | ✅ Yes          | ✓ DONE                  | krandom: ObjectGeneratorConfig.maxDepth()     |
| Object pool size      | ✅ `objectPoolSize(int)` default: 10            | ❌ No           | MEDIUM                  | Recursion guard                               |
| **Date/Time Ranges**  |
| Date range            | ✅ `dateRange(LocalDate, LocalDate)`            | ❌ No           | MEDIUM                  | [2010-01-01, 2030-01-01]                      |
| Time range            | ✅ `timeRange(LocalTime, LocalTime)`            | ❌ No           | MEDIUM                  |                                               |
| **Behavioral Flags**  |
| Override defaults     | ✅ `overrideDefaultInitialization(bool)`        | ❌ No           | LOW                     | Re-randomize initialized fields               |
| Ignore errors         | ✅ `ignoreRandomizationErrors(bool)`            | ✅ Yes          | ✓ DONE                  | krandom: ObjectGeneratorConfig.ignoreErrors() |
| Bypass setters        | ✅ `bypassSetters(bool)`                        | ✅ Yes          | ✓ DONE                  | Direct field access                           |
| Scan classpath        | ✅ `scanClasspathForConcreteTypes(bool)`        | ❌ No           | LOW                     | Find implementations                          |
| **Builder Pattern**   |
| Fluent API            | ✅ All setters return `this`                    | ✅ Yes          | ✓ DONE                  | Both use builders                             |

### 6. BUILT-IN RANDOMIZERS

| Feature                           | Easy Random Support                              | krandom Status | Implementation Priority | Notes                          |
|-----------------------------------|--------------------------------------------------|----------------|-------------------------|--------------------------------|
| **Primitive & Boxed Types**       |
| boolean / Boolean                 | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  |                                |
| byte / Byte                       | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  |                                |
| short / Short                     | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  |                                |
| int / Integer                     | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  |                                |
| long / Long                       | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  |                                |
| float / Float                     | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  |                                |
| double / Double                   | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  |                                |
| char / Character                  | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  |                                |
| **Numeric Types**                 |
| BigInteger                        | ✅ Yes                                            | ❌ No           | MEDIUM                  | Arbitrary precision            |
| BigDecimal                        | ✅ Yes                                            | ❌ No           | MEDIUM                  | Decimal arithmetic             |
| AtomicInteger                     | ✅ Yes                                            | ❌ No           | LOW                     | Concurrent types               |
| AtomicLong                        | ✅ Yes                                            | ❌ No           | LOW                     |                                |
| **Range Randomizers**             |
| ByteRangeRandomizer               | ✅ `(min, max)`                                   | ✅ Yes          | ✓ DONE                  | krandom: BoundedGenerator      |
| ShortRangeRandomizer              | ✅ `(min, max)`                                   | ✅ Yes          | ✓ DONE                  |                                |
| IntegerRangeRandomizer            | ✅ `(min, max)`                                   | ✅ Yes          | ✓ DONE                  |                                |
| LongRangeRandomizer               | ✅ `(min, max)`                                   | ✅ Yes          | ✓ DONE                  |                                |
| FloatRangeRandomizer              | ✅ `(min, max)`                                   | ✅ Yes          | ✓ DONE                  |                                |
| DoubleRangeRandomizer             | ✅ `(min, max)`                                   | ✅ Yes          | ✓ DONE                  |                                |
| BigDecimalRangeRandomizer         | ✅ `(min, max)`                                   | ❌ No           | MEDIUM                  |                                |
| BigIntegerRangeRandomizer         | ✅ `(min, max)`                                   | ❌ No           | MEDIUM                  |                                |
| **String Types**                  |
| String                            | ✅ Random ASCII                                   | ✅ Yes          | ✓ DONE                  |                                |
| StringRandomizer                  | ✅ Custom length                                  | ✅ Yes          | ✓ DONE                  |                                |
| GenericStringRandomizer           | ✅ DataFaker-backed                               | ❌ No           | LOW                     |                                |
| RegularExpressionRandomizer       | ✅ Regex-based                                    | ❌ No           | HIGH                    | Pattern matching               |
| **Standard Library Types**        |
| UUID                              | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  | krandom: UUIDGenerator         |
| Locale                            | ✅ Yes                                            | ❌ No           | MEDIUM                  | Random locale                  |
| URI                               | ✅ Yes                                            | ✅ Partial      | MEDIUM                  | krandom: UrlGenerator (URLs)   |
| URL                               | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  | krandom: UrlGenerator          |
| **Date/Time (Legacy)**            |
| java.util.Date                    | ✅ Yes                                            | ❌ No           | MEDIUM                  | Legacy support                 |
| java.util.Calendar                | ✅ Yes                                            | ❌ No           | MEDIUM                  |                                |
| java.util.GregorianCalendar       | ✅ Yes                                            | ❌ No           | LOW                     |                                |
| java.sql.Date                     | ✅ Yes                                            | ❌ No           | MEDIUM                  | SQL types                      |
| java.sql.Time                     | ✅ Yes                                            | ❌ No           | MEDIUM                  |                                |
| java.sql.Timestamp                | ✅ Yes                                            | ❌ No           | MEDIUM                  |                                |
| java.util.TimeZone                | ✅ Yes                                            | ❌ No           | LOW                     |                                |
| **Date/Time (JSR 310)**           |
| Instant                           | ✅ Yes                                            | ❌ No           | MEDIUM                  | Java 8 time                    |
| LocalDate                         | ✅ Yes                                            | ❌ No           | HIGH                    | Date without time              |
| LocalTime                         | ✅ Yes                                            | ❌ No           | HIGH                    | Time without date              |
| LocalDateTime                     | ✅ Yes                                            | ❌ No           | HIGH                    | Combined                       |
| OffsetDateTime                    | ✅ Yes                                            | ❌ No           | MEDIUM                  | With timezone offset           |
| OffsetTime                        | ✅ Yes                                            | ❌ No           | MEDIUM                  |                                |
| ZonedDateTime                     | ✅ Yes                                            | ❌ No           | MEDIUM                  | With timezone                  |
| Year                              | ✅ Yes                                            | ❌ No           | MEDIUM                  |                                |
| YearMonth                         | ✅ Yes                                            | ❌ No           | MEDIUM                  |                                |
| MonthDay                          | ✅ Yes                                            | ❌ No           | LOW                     |                                |
| Duration                          | ✅ Yes                                            | ❌ No           | MEDIUM                  | Time spans                     |
| Period                            | ✅ Yes                                            | ❌ No           | MEDIUM                  | Date periods                   |
| ZoneId                            | ✅ Yes                                            | ❌ No           | LOW                     | Timezone IDs                   |
| ZoneOffset                        | ✅ Yes                                            | ❌ No           | LOW                     |                                |
| **Range Randomizers (Date/Time)** |
| DateRangeRandomizer               | ✅ `(min, max)`                                   | ❌ No           | MEDIUM                  | Legacy dates                   |
| LocalDateRangeRandomizer          | ✅ `(min, max)`                                   | ❌ No           | HIGH                    | Date ranges                    |
| LocalDateTimeRangeRandomizer      | ✅ `(min, max)`                                   | ❌ No           | HIGH                    |                                |
| LocalTimeRangeRandomizer          | ✅ `(min, max)`                                   | ❌ No           | MEDIUM                  |                                |
| InstantRangeRandomizer            | ✅ `(min, max)`                                   | ❌ No           | MEDIUM                  |                                |
| OffsetDateTimeRangeRandomizer     | ✅ `(min, max)`                                   | ❌ No           | MEDIUM                  |                                |
| ZonedDateTimeRangeRandomizer      | ✅ `(min, max)`                                   | ❌ No           | MEDIUM                  |                                |
| YearRangeRandomizer               | ✅ `(min, max)`                                   | ❌ No           | LOW                     |                                |
| **Enums**                         |
| Enum randomization                | ✅ Random constant                                | ✅ Yes          | ✓ DONE                  | krandom: EnumGenerator         |
| EnumRandomizer<T>                 | ✅ Generic                                        | ✅ Yes          | ✓ DONE                  |                                |
| **Arrays**                        |
| Array population                  | ✅ Within collectionSizeRange                     | ❌ No           | HIGH                    | krandom doesn't support arrays |
| **Collections (JCF)**             |
| List / ArrayList                  | ✅ Yes                                            | ❌ No           | HIGH                    | Generic collection support     |
| LinkedList                        | ✅ Yes                                            | ❌ No           | MEDIUM                  |                                |
| Set / HashSet                     | ✅ Yes                                            | ❌ No           | HIGH                    |                                |
| LinkedHashSet                     | ✅ Yes                                            | ❌ No           | MEDIUM                  |                                |
| TreeSet                           | ✅ Yes                                            | ❌ No           | MEDIUM                  |                                |
| Queue / ArrayDeque                | ✅ Yes                                            | ❌ No           | MEDIUM                  |                                |
| PriorityQueue                     | ✅ Yes                                            | ❌ No           | LOW                     |                                |
| Collection randomizers            | ✅ ListRandomizer, SetRandomizer, QueueRandomizer | ❌ No           | HIGH                    | Dedicated randomizers          |
| EnumSet                           | ✅ EnumSetRandomizer                              | ❌ No           | LOW                     |                                |
| **Maps**                          |
| Map / HashMap                     | ✅ Yes                                            | ❌ No           | HIGH                    | Key-value pairs                |
| LinkedHashMap                     | ✅ Yes                                            | ❌ No           | MEDIUM                  |                                |
| TreeMap                           | ✅ Yes                                            | ❌ No           | MEDIUM                  |                                |
| Hashtable                         | ✅ Yes                                            | ❌ No           | LOW                     | Legacy                         |
| WeakHashMap                       | ✅ Yes                                            | ❌ No           | LOW                     |                                |
| IdentityHashMap                   | ✅ Yes                                            | ❌ No           | LOW                     |                                |
| EnumMap                           | ✅ Yes                                            | ❌ No           | LOW                     |                                |
| MapRandomizer                     | ✅ `MapRandomizer<K,V>`                           | ❌ No           | HIGH                    | Dedicated randomizer           |
| **Optional**                      |
| Optional<T>                       | ✅ OptionalPopulator                              | ❌ No           | MEDIUM                  | Java 8 optionals               |
| OptionalRandomizer                | ✅ Yes                                            | ❌ No           | MEDIUM                  |                                |
| **Utility Randomizers**           |
| ConstantRandomizer                | ✅ Always same value                              | ❌ No           | MEDIUM                  | Fixed value generator          |
| NullRandomizer                    | ✅ Always null                                    | ❌ No           | LOW                     |                                |
| SkipRandomizer                    | ✅ Leave field unset                              | ❌ No           | LOW                     | Null object pattern            |

### 7. REALISTIC DATA (DataFaker Integration)

| Feature           | Easy Random Support            | krandom Status | Implementation Priority | Notes                                          |
|-------------------|--------------------------------|----------------|-------------------------|------------------------------------------------|
| **Personal Data** |
| First name        | ✅ `FirstNameRandomizer`        | ✅ Yes          | ✓ DONE                  | krandom: FirstNameGenerator (10 locales)       |
| Last name         | ✅ `LastNameRandomizer`         | ✅ Yes          | ✓ DONE                  | krandom: LastNameGenerator (10 locales)        |
| Full name         | ✅ `FullNameRandomizer`         | ✅ Partial      | MEDIUM                  | Compose First+Last; no single FullNameGenerator|
| Email             | ✅ `EmailRandomizer`            | ✅ Yes          | ✓ DONE                  | krandom: EmailGenerator (7 formats)            |
| Phone number      | ✅ `PhoneNumberRandomizer`      | ✅ Yes          | ✓ DONE                  | krandom: PhoneNumberGenerator (10 locales)     |
| Password          | ✅ `PasswordRandomizer`         | ❌ No           | MEDIUM                  |                                                |
| **Address Data**  |
| Street            | ✅ `StreetRandomizer`           | ❌ No           | MEDIUM                  |                                                |
| City              | ✅ `CityRandomizer`             | ✅ Yes          | ✓ DONE                  | krandom: CityGenerator (10 locales)            |
| State             | ✅ `StateRandomizer`            | ✅ Yes          | ✓ DONE                  | krandom: StateGenerator (10 locales, full+abbrev)|
| Country           | ✅ `CountryRandomizer`          | ✅ Yes          | ✓ DONE                  | krandom: CountryGenerator (10 locales)         |
| ZIP code          | ✅ `ZipCodeRandomizer`          | ✅ Yes          | ✓ DONE                  | krandom: PostalCodeGenerator (10 locales)      |
| Latitude          | ✅ `LatitudeRandomizer`         | ✅ Yes          | ✓ DONE                  | krandom: CoordinateGenerator.latitude()        |
| Longitude         | ✅ `LongitudeRandomizer`        | ✅ Yes          | ✓ DONE                  | krandom: CoordinateGenerator.longitude()       |
| **Company Data**  |
| Company name      | ✅ `CompanyRandomizer`          | ❌ No           | MEDIUM                  |                                                |
| **Text Data**     |
| Paragraph         | ✅ `ParagraphRandomizer`        | ❌ No           | MEDIUM                  | Lorem ipsum                                    |
| Sentence          | ✅ `SentenceRandomizer`         | ❌ No           | MEDIUM                  |                                                |
| Word              | ✅ `WordRandomizer`             | ❌ No           | MEDIUM                  |                                                |
| **Network Data**  |
| IPv4 address      | ✅ `Ipv4AddressRandomizer`      | ✅ Yes          | ✓ DONE                  | krandom: IPv4Generator                         |
| IPv6 address      | ✅ `Ipv6AddressRandomizer`      | ✅ Yes          | ✓ DONE                  | krandom: IPv6Generator                         |
| MAC address       | ✅ `MacAddressRandomizer`       | ❌ No           | MEDIUM                  | Hardware addresses                             |
| **Product Data**  |
| ISBN              | ✅ `IsbnRandomizer`             | ❌ No           | MEDIUM                  | Book codes                                     |
| Credit card       | ✅ `CreditCardNumberRandomizer` | ✅ Yes          | ✓ DONE                  | krandom: CreditCardGenerator (Luhn-valid)      |

### 8. CLASSPATH SCANNING

| Feature                      | Easy Random Support                     | krandom Status | Implementation Priority | Notes                   |
|------------------------------|-----------------------------------------|----------------|-------------------------|-------------------------|
| Abstract class instantiation | ✅ Random concrete subtype               | ❌ No           | LOW                     | Requires scanning       |
| Interface instantiation      | ✅ Random implementation                 | ❌ No           | LOW                     |                         |
| Enable scanning              | ✅ `scanClasspathForConcreteTypes(true)` | ❌ No           | LOW                     | Uses ClassGraph library |
| Performance                  | ⚠️ Slow on large classpaths             | ❌ No           | LOW                     | Runtime overhead        |

### 9. ADVANCED FEATURES

| Feature                        | Easy Random Support                   | krandom Status        | Implementation Priority | Notes                       |
|--------------------------------|---------------------------------------|-----------------------|-------------------------|-----------------------------|
| **Recursion Control**          |
| Circular ref detection         | ✅ Object pool caching                 | ❌ No                  | HIGH                    | Prevent stack overflow      |
| Max depth guard                | ✅ `randomizationDepth(int)`           | ✅ Yes                 | ✓ DONE                  | Both support                |
| Pool size config               | ✅ `objectPoolSize(int)`               | ❌ No                  | MEDIUM                  | Cache size control          |
| **Error Handling**             |
| Ignore errors                  | ✅ `ignoreRandomizationErrors(true)`   | ✅ Yes                 | ✓ DONE                  | Silently set null           |
| Throw exceptions               | ✅ `ignoreRandomizationErrors(false)`  | ✅ Default             | ✓ DONE                  | Fail fast                   |
| **Inner Classes**              |
| Static nested classes          | ✅ Yes                                 | ✅ Yes                 | ✓ DONE                  | Fully supported             |
| Non-static inner classes       | ⚠️ Cannot instantiate                 | ⚠️ Cannot instantiate | N/A                     | Requires enclosing instance |
| **Extension Points**           |
| RandomizerProvider             | ✅ Custom resolution strategy          | ❌ No                  | MEDIUM                  | Algorithm customization     |
| RandomizerRegistry             | ✅ Group randomizers                   | ❌ No                  | MEDIUM                  |                             |
| ExclusionPolicy                | ✅ Custom exclusion logic              | ❌ No                  | MEDIUM                  |                             |
| ObjectFactory                  | ✅ Custom instantiation                | ❌ No                  | MEDIUM                  |                             |
| **Service Provider Interface** |
| ServiceLoader discovery        | ✅ `META-INF/services/`                | ❌ No                  | LOW                     | Auto-load registries        |
| **Known Limitations**          |
| Type erasure                   | ⚠️ Limited nested generics            | ⚠️ Same issue         | N/A                     | JVM limitation              |
| Android support                | ❌ Objenesis/ClassGraph not compatible | N/A                   | N/A                     | Desktop JVM only            |
| javax.xml.datatype             | ❌ Not supported                       | ❌ Not supported       | LOW                     | XMLGregorianCalendar        |
| @Digits constraint             | ❌ Not in BeanValidation module        | ❌ No                  | LOW                     |                             |

---

## ADVANCED FEATURES COMPARISON

### Object Generation Architecture

| Aspect              | Easy Random                          | krandom                     | Winner      |
|---------------------|--------------------------------------|-----------------------------|-------------|
| **Instantiation**   | Objenesis (no-arg not required)      | No-arg constructor required | Easy Random |
| **Recursion guard** | Object pool (configurable size)      | Depth limit only            | Easy Random |
| **Circular refs**   | Handled via pool                     | ❌ Stack overflow risk       | Easy Random |
| **Inheritance**     | Full hierarchy walked                | Full hierarchy walked       | Tie         |
| **Field access**    | Direct reflection + optional setters | Direct reflection           | Tie         |
| **Generic types**   | Limited nested generics              | Basic support               | Tie         |
| **Records**         | Canonical constructor                | Canonical constructor       | Tie         |
| **Abstract types**  | Classpath scanning for impls         | Returns null                | Easy Random |

### Customization Flexibility

| Aspect                      | Easy Random                                          | krandom                      | Winner      |
|-----------------------------|------------------------------------------------------|------------------------------|-------------|
| **Per-type customization**  | ✅ `randomize(Class, randomizer)`                     | ✅ `typeOverrides`            | Tie         |
| **Per-field customization** | ✅ Predicate-based + @Randomizer                      | ✅ `fieldOverrides` (by name) | Easy Random |
| **Exclusion mechanism**     | ✅ Predicate-based + @Exclude                         | ❌ No built-in                | Easy Random |
| **Context awareness**       | ✅ `ContextAwareRandomizer`                           | ❌ No context                 | Easy Random |
| **Registry system**         | ✅ Layered with priority                              | ❌ No registry                | Easy Random |
| **Extension points**        | ✅ 4 interfaces (Provider, Registry, Policy, Factory) | ❌ Limited                    | Easy Random |

### Bean Validation Support

| Aspect                  | Easy Random                                                   | krandom         | Winner      |
|-------------------------|---------------------------------------------------------------|-----------------|-------------|
| **Constraint support**  | ✅ 10+ annotations (`@NotNull`, `@Size`, `@Min`, `@Max`, etc.) | ❌ No validation | Easy Random |
| **Separate module**     | ✅ `easy-random-bean-validation`                               | N/A             | Easy Random |
| **Constraint priority** | ✅ Overrides global config                                     | N/A             | Easy Random |

### Collection Support

| Aspect           | Easy Random                       | krandom                            | Winner      |
|------------------|-----------------------------------|------------------------------------|-------------|
| **Arrays**       | ✅ Populated with elements         | ❌ Not supported                    | Easy Random |
| **List types**   | ✅ ArrayList, LinkedList           | ❌ generateList() returns immutable | Easy Random |
| **Set types**    | ✅ HashSet, TreeSet, LinkedHashSet | ❌ No                               | Easy Random |
| **Map types**    | ✅ HashMap, TreeMap, etc.          | ❌ No                               | Easy Random |
| **Queue types**  | ✅ ArrayDeque, PriorityQueue       | ❌ No                               | Easy Random |
| **Size control** | ✅ `collectionSizeRange(min, max)` | ✅ `collectionSize(min, max)`       | Tie         |

### Realistic Data Generation

| Aspect                    | Easy Random                    | krandom                                         | Winner      |
|---------------------------|--------------------------------|-------------------------------------------------|-------------|
| **DataFaker integration** | ✅ 20+ realistic randomizers    | ✅ 50+ built-in generators (no extra dep)        | krandom     |
| **Personal data**         | ✅ Names, emails, phones        | ✅ Names, email, phone, birthday, national IDs   | Tie         |
| **Address data**          | ✅ Streets, cities, states, ZIP | ✅ City, state, country, ZIP, coordinates        | Easy Random |
| **Finance data**          | ❌ No                          | ✅ Credit cards, money, currency pairs, FX pairs | krandom     |
| **Network data**          | ✅ IPv4, IPv6, MAC              | ✅ IPv4, IPv6 only                               | Easy Random |
| **Company data**          | ✅ Company names                | ❌ No                                            | Easy Random |
| **Text generation**       | ✅ Paragraphs, sentences, words | ❌ Random strings only                           | Easy Random |
| **Locale support**        | ✅ Via DataFaker                | ✅ 10 locales across all realistic generators    | Tie         |

### Configuration & Defaults

| Aspect              | Easy Random         | krandom                 | Winner                      |
|---------------------|---------------------|-------------------------|-----------------------------|
| **Seed**            | ✅ Default: 123L     | ✅ Default: SecureRandom | Easy Random (deterministic) |
| **Charset**         | ✅ Default: US_ASCII | ✅ Default: US_ASCII     | Tie                         |
| **String length**   | ✅ [1, 32]           | ✅ [5, 20]               | Preference                  |
| **Collection size** | ✅ [1, 100]          | ✅ [1, 10]               | Preference                  |
| **Max depth**       | ✅ Integer.MAX_VALUE | ✅ 5                     | krandom (safer)             |
| **Builder API**     | ✅ Fluent            | ✅ Fluent                | Tie                         |

---

## IMPLEMENTATION RECOMMENDATIONS

### Phase 1: CRITICAL OBJECT GENERATION GAPS (Must Have)

**1. Circular Reference Handling** ⚡ HIGH PRIORITY

- **Gap**: krandom can stack overflow on circular object graphs
- **Easy Random approach**: Object pool caching (default size: 10)
- **Implementation**:
  ```java
  class ObjectPool {
      private Map<Class<?>, Queue<Object>> pool;
      private int maxSize;

      Object getOrCreate(Class<?> type, Supplier<Object> factory) {
          Queue<Object> instances = pool.get(type);
          if (instances != null && !instances.isEmpty()) {
              return instances.poll();
          }
          if (poolIsFull(type)) {
              return instances.peek(); // Return cached instance
          }
          Object instance = factory.get();
          pool.computeIfAbsent(type, k -> new ArrayDeque<>()).offer(instance);
          return instance;
      }
  }
  ```
- **Effort**: 2 days
- **Value**: Prevents crashes on complex object graphs

**2. Array Type Support** ⚡ HIGH PRIORITY

- **Gap**: krandom doesn't populate arrays (returns null)
- **Easy Random approach**: Detect array type, create with collectionSizeRange length
- **Implementation**:
  ```kotlin
  class ArrayGenerator<T>(private val componentType: Class<T>) : Generator<Array<T>> {
      override fun generate(): Array<T> {
          val size = config.collectionSize.random()
          val componentGen = Generators.forType(componentType)
          return Array(size) { componentGen.generate() }
      }
  }
  ```
- **Effort**: 1 day
- **Value**: Complete object graph population

**3. Collection Type Support** ⚡ HIGH PRIORITY

- **Gap**: krandom doesn't auto-populate List/Set/Map fields
- **Easy Random approach**: Detect collection type, instantiate concrete impl, populate
- **Implementation**:
  ```kotlin
  class CollectionPopulator {
      fun populate(field: Field, context: GeneratorContext): Any? {
          val fieldType = field.type
          val elementType = resolveElementType(field.genericType)
          val size = config.collectionSize.random()

          return when {
              List::class.java.isAssignableFrom(fieldType) ->
                  List(size) { Generators.forType(elementType).generate() }
              Set::class.java.isAssignableFrom(fieldType) ->
                  Set(size) { Generators.forType(elementType).generate() }
              Map::class.java.isAssignableFrom(fieldType) -> {
                  val (keyType, valueType) = resolveMapTypes(field.genericType)
                  Map(size) {
                      Generators.forType(keyType).generate() to
                      Generators.forType(valueType).generate()
                  }
              }
              else -> null
          }
      }
  }
  ```
- **Effort**: 3 days
- **Value**: Essential for realistic object graphs

**4. Objenesis Integration** ⚠️ MEDIUM PRIORITY

- **Gap**: krandom requires no-arg constructor; fails on Lombok @AllArgsConstructor, immutable classes
- **Easy Random approach**: Try no-arg first, fall back to Objenesis
- **Implementation**:
  ```kotlin
  class ObjenesisObjectFactory : ObjectFactory {
      private val objenesis = ObjenesisStd()

      fun <T> create(clazz: Class<T>): T {
          return try {
              clazz.getDeclaredConstructor().newInstance()
          } catch (e: NoSuchMethodException) {
              objenesis.newInstance(clazz)
          }
      }
  }
  ```
- **Effort**: 1 day
- **Value**: Broader compatibility with library classes, DTOs

**Total Phase 1: ~7 days**

---

### Phase 2: CUSTOMIZATION & EXCLUSION (Should Have)

**1. Field Exclusion API** ⚡ HIGH PRIORITY

- **Gap**: No way to skip specific fields (e.g., `password`, `transient` metadata)
- **Easy Random approach**: Predicate-based + @Exclude annotation
- **Implementation**:
  ```kotlin
  @Target(AnnotationTarget.FIELD)
  annotation class Exclude

  class FieldPredicate {
      companion object {
          fun named(pattern: String): (Field) -> Boolean =
              { it.name.matches(Regex(pattern)) }

          fun ofType(type: Class<*>): (Field) -> Boolean =
              { it.type == type }

          fun inClass(clazz: Class<*>): (Field) -> Boolean =
              { it.declaringClass == clazz }

          fun annotatedWith(annotation: Class<out Annotation>): (Field) -> Boolean =
              { it.isAnnotationPresent(annotation) }
      }
  }

  class ObjectGeneratorConfig {
      private val excludedFields = mutableListOf<(Field) -> Boolean>()

      fun excludeField(predicate: (Field) -> Boolean): ObjectGeneratorConfig {
          excludedFields.add(predicate)
          return this
      }

      fun shouldExclude(field: Field): Boolean {
          return field.isAnnotationPresent(Exclude::class.java) ||
                 excludedFields.any { it(field) }
      }
  }
  ```
- **Effort**: 2 days
- **Value**: Fine-grained control over generation

**2. @Randomizer Annotation** ⚠️ MEDIUM PRIORITY

- **Gap**: Currently must use API; annotation is more declarative
- **Easy Random approach**: `@Randomizer(EmailRandomizer::class)` on field
- **Implementation**:
  ```kotlin
  @Target(AnnotationTarget.FIELD)
  annotation class Randomizer(
      val value: KClass<out Generator<*>>,
      val args: Array<RandomizerArgument> = []
  )

  @Repeatable
  annotation class RandomizerArgument(
      val value: String,
      val type: KClass<*>
  )

  // Usage:
  class Person {
      @Randomizer(EmailGenerator::class)
      lateinit var email: String

      @Randomizer(
          value = IntGenerator::class,
          args = [
              RandomizerArgument("18", Int::class),
              RandomizerArgument("65", Int::class)
          ]
      )
      var age: Int = 0
  }
  ```
- **Effort**: 2 days
- **Value**: Declarative configuration, self-documenting code

**3. Context-Aware Generators** ⚠️ MEDIUM PRIORITY

- **Gap**: Generators can't access field path, depth, parent object
- **Easy Random approach**: `ContextAwareRandomizer` with `RandomizerContext`
- **Implementation**:
  ```kotlin
  interface GeneratorContext {
      val targetType: Class<*>
      val rootObject: Any?
      val currentObject: Any?
      val currentFieldPath: String // "person.address.city"
      val currentDepth: Int
      val config: ObjectGeneratorConfig
  }

  interface ContextAwareGenerator<T> : Generator<T> {
      fun setContext(context: GeneratorContext)
  }

  // Example: Generate email based on name field
  class EmailGenerator : ContextAwareGenerator<String> {
      private lateinit var context: GeneratorContext

      override fun setContext(context: GeneratorContext) {
          this.context = context
      }

      override fun generate(): String {
          val obj = context.currentObject
          val name = obj?.javaClass?.getDeclaredField("name")?.get(obj)
          return "${name}@example.com".lowercase()
      }
  }
  ```
- **Effort**: 2 days
- **Value**: Enables sophisticated cross-field generation

**Total Phase 2: ~6 days**

---

### Phase 3: BEAN VALIDATION INTEGRATION (Could Have)

**1. JSR 380 Constraint Support** ⚠️ MEDIUM PRIORITY

- **Gap**: No validation annotation support
- **Easy Random approach**: Separate module `easy-random-bean-validation`
- **Supported constraints**: `@NotNull`, `@NotEmpty`, `@Size`, `@Min`, `@Max`, `@Past`, `@Future`, `@Email`, `@Pattern`
- **Implementation**:
  ```kotlin
  class BeanValidationRegistry : GeneratorRegistry {
      override fun getGenerator(field: Field): Generator<*>? {
          return when {
              field.isAnnotationPresent(NotNull::class.java) ->
                  NonNullGenerator(baseGenerator(field))

              field.isAnnotationPresent(Size::class.java) -> {
                  val size = field.getAnnotation(Size::class.java)
                  when (field.type) {
                      String::class.java -> StringGenerator(size.min, size.max)
                      Collection::class.java -> CollectionGenerator(size.min, size.max)
                      else -> null
                  }
              }

              field.isAnnotationPresent(Min::class.java) -> {
                  val min = field.getAnnotation(Min::class.java).value
                  IntGenerator(min.toInt(), Int.MAX_VALUE)
              }

              field.isAnnotationPresent(Max::class.java) -> {
                  val max = field.getAnnotation(Max::class.java).value
                  IntGenerator(Int.MIN_VALUE, max.toInt())
              }

              field.isAnnotationPresent(Email::class.java) ->
                  EmailGenerator()

              field.isAnnotationPresent(Pattern::class.java) -> {
                  val regex = field.getAnnotation(Pattern::class.java).regexp
                  RegexGenerator(regex)
              }

              else -> null
          }
      }
  }
  ```
- **Effort**: 5 days (separate module)
- **Value**: Auto-validates test data, reduces boilerplate

**Total Phase 3: ~5 days**

---

### Phase 4: DATE/TIME GENERATORS (Should Have)

**1. JSR 310 (java.time) Randomizers** ⚠️ MEDIUM PRIORITY

- **Gap**: No date/time type support
- **Easy Random approach**: Dedicated randomizers for each type + range versions
- **Types to support**:
    - `LocalDate`, `LocalTime`, `LocalDateTime`
    - `Instant`, `ZonedDateTime`, `OffsetDateTime`
    - `Year`, `YearMonth`, `Duration`, `Period`
- **Implementation**:
  ```kotlin
  class LocalDateGenerator(
      private val min: LocalDate = LocalDate.of(2010, 1, 1),
      private val max: LocalDate = LocalDate.of(2030, 1, 1)
  ) : Generator<LocalDate> {
      override fun generate(): LocalDate {
          val start = min.toEpochDay()
          val end = max.toEpochDay()
          val randomDay = Random.nextLong(start, end + 1)
          return LocalDate.ofEpochDay(randomDay)
      }
  }

  class LocalDateTimeGenerator(
      private val minDate: LocalDate = LocalDate.of(2010, 1, 1),
      private val maxDate: LocalDate = LocalDate.of(2030, 1, 1),
      private val minTime: LocalTime = LocalTime.MIN,
      private val maxTime: LocalTime = LocalTime.MAX
  ) : Generator<LocalDateTime> {
      override fun generate(): LocalDateTime {
          val date = LocalDateGenerator(minDate, maxDate).generate()
          val time = LocalTimeGenerator(minTime, maxTime).generate()
          return LocalDateTime.of(date, time)
      }
  }
  ```
- **Effort**: 3 days
- **Value**: Essential for domain objects with timestamps

**2. Date Range Configuration** ⚠️ MEDIUM PRIORITY

- **Gap**: No global date range configuration
- **Easy Random approach**: `dateRange(LocalDate, LocalDate)`, `timeRange(LocalTime, LocalTime)`
- **Implementation**:
  ```kotlin
  class GeneratorConfig {
      var dateRange: ClosedRange<LocalDate> =
          LocalDate.of(2010, 1, 1)..LocalDate.of(2030, 1, 1)
      var timeRange: ClosedRange<LocalTime> =
          LocalTime.MIN..LocalTime.MAX

      fun dateRange(min: LocalDate, max: LocalDate): GeneratorConfig {
          dateRange = min..max
          return this
      }

      fun timeRange(min: LocalTime, max: LocalTime): GeneratorConfig {
          timeRange = min..max
          return this
      }
  }
  ```
- **Effort**: 1 day
- **Value**: Consistent date generation across app

**Total Phase 4: ~4 days**

---

### Phase 5: REALISTIC DATA GENERATORS (Nice to Have)

**1. Email Generator** ~~DONE~~ ✅

- **Implemented**: `EmailGenerator` (7 formats: simple, domain-specific, first.last, UUID-based, safe, custom domain, locale-aware)

**2. UUID Generator** ~~DONE~~ ✅

- **Implemented**: `UUIDGenerator` (UUID v4 with seeded support)

**3. Regex-Based Generator** ⚡ HIGH PRIORITY

- **Gap**: Can't generate from patterns
- **Easy Random approach**: `RegularExpressionRandomizer`
- **Implementation**: Use [generex](https://github.com/mifmif/Generex) library
  ```kotlin
  class RegexGenerator(private val pattern: String) : Generator<String> {
      private val generex = Generex(pattern)

      override fun generate(): String = generex.random()
  }
  ```
- **Effort**: 1 day (+ dependency)
- **Value**: Flexible pattern-based generation

**4. Lorem Ipsum Generators** ⚠️ MEDIUM PRIORITY

- **Gap**: No text content generation
- **Easy Random approach**: `WordRandomizer`, `SentenceRandomizer`, `ParagraphRandomizer`
- **Implementation**:
  ```kotlin
  class WordGenerator : Generator<String> {
      private val words = listOf("lorem", "ipsum", "dolor", "sit", "amet", ...)
      override fun generate(): String = words.random()
  }

  class SentenceGenerator(private val wordCount: IntRange = 5..15) : Generator<String> {
      override fun generate(): String {
          val count = wordCount.random()
          return List(count) { WordGenerator().generate() }
              .joinToString(" ")
              .capitalize() + "."
      }
  }

  class ParagraphGenerator(private val sentenceCount: IntRange = 3..7) : Generator<String> {
      override fun generate(): String {
          val count = sentenceCount.random()
          return List(count) { SentenceGenerator().generate() }.joinToString(" ")
      }
  }
  ```
- **Effort**: 1 day
- **Value**: UI/content testing

**5. Name Generators** ~~DONE~~ ✅

- **Implemented**: `FirstNameGenerator` + `LastNameGenerator` (10 locales, gender-aware)

**6. Address Components** ~~DONE~~ ✅

- **Implemented**: `CityGenerator`, `StateGenerator`, `CountryGenerator`, `PostalCodeGenerator`, `CoordinateGenerator` (all 10 locales)

**7. Credit Card Generator** ~~DONE~~ ✅

- **Implemented**: `CreditCardGenerator` (Visa/MC/Amex/Discover, Luhn-valid, with CVV and expiry generators)

**Total Phase 5: ~~7 days~~ Completed ✅**

---

### Phase 6: ADVANCED FEATURES (Low Priority)

**1. BigDecimal / BigInteger Generators**

- **Effort**: 1 day
- **Value**: Financial calculations

**2. Optional<T> Support**

- **Effort**: 0.5 days
- **Value**: Java 8+ null safety

**3. Classpath Scanning for Abstract Types**

- **Effort**: 3 days (+ ClassGraph dependency)
- **Value**: Limited (slow, complex)

**4. ServiceLoader Registry Discovery**

- **Effort**: 2 days
- **Value**: Plugin architecture

**Total Phase 6: ~6.5 days**

---

## EFFORT ESTIMATES SUMMARY

| Phase     | Focus Area                                                             | Effort                   | Priority   | Status       |
|-----------|------------------------------------------------------------------------|--------------------------|------------|--------------|
| Phase 1   | Object generation gaps (circular refs, arrays, collections, Objenesis) | 7 days                   | ⚡ CRITICAL | ❌ Pending   |
| Phase 2   | Customization & exclusion (predicates, @Randomizer, context)           | 6 days                   | HIGH       | ❌ Pending   |
| Phase 3   | Bean validation integration                                            | 5 days                   | MEDIUM     | ❌ Pending   |
| Phase 4   | Date/time generators                                                   | 4 days                   | MEDIUM     | ❌ Pending   |
| Phase 5   | Realistic data (email, UUID, regex, lorem, names, addresses, cards)    | 7 days                   | MEDIUM     | ✅ DONE      |
| Phase 6   | Advanced features (BigDecimal, Optional, scanning, SPI)                | 6.5 days                 | LOW        | ❌ Pending   |
| **TOTAL** |                                                                        | **28.5 days** (~6 weeks) |            |              |

### Recommended Implementation Order

1. **Week 1-2**: Phase 1 (critical gaps) + Phase 4 (dates — now top priority since Phase 5 done)
2. **Week 3-4**: Phase 2 (customization) + Phase 3 (validation)
3. **Week 5**: Phase 6 (nice-to-have) + documentation

---

## KEY DIFFERENTIATORS

### Easy Random Strengths (vs krandom)

1. **Object Pool for Circular References** 🏆
    - Prevents stack overflow on self-referential objects
    - Configurable pool size (default: 10)
    - krandom will crash on: `Person.spouse -> Person.spouse -> ...`

2. **Objenesis Integration** 🏆
    - Instantiates objects without constructors
    - Works with Lombok `@AllArgsConstructor`, immutable classes
    - krandom requires no-arg constructor

3. **Array & Collection Auto-Population** 🏆
    - Automatically populates `List<T>`, `Set<T>`, `Map<K,V>`, `T[]` fields
    - krandom returns null for these types

4. **Bean Validation Support** 🏆
    - Generates data satisfying `@NotNull`, `@Size`, `@Min`, `@Max`, etc.
    - Separate module: `easy-random-bean-validation`
    - krandom has no validation awareness

5. **Exclusion Mechanisms** 🏆
    - Field predicates: `named("password")`, `ofType(Secret.class)`, `inClass(User.class)`
    - `@Exclude` annotation
    - Custom `ExclusionPolicy` interface
    - krandom has no built-in exclusion

6. **Predicate-Based Customization** 🏆
    - Target specific fields: `FieldPredicates.named("email").and(inClass(Person.class))`
    - Composable: `and()`, `or()`, `negate()`
    - krandom uses simple string matching

7. **Context-Aware Generators** 🏆
    - `ContextAwareRandomizer` receives `RandomizerContext`
    - Access: target type, root object, current object, field path, depth
    - Enables cross-field generation (e.g., email from name)
    - krandom generators are isolated

8. **Layered Registry System** 🏆
    - 6 built-in registries with priority order
    - User registries override built-ins via `@Priority`
    - ServiceLoader auto-discovery
    - krandom has simple override maps

9. **Annotation-Based Configuration** 🏆
    - `@Randomizer(EmailRandomizer.class)` on fields
    - `@RandomizerArgument` for constructor params
    - Self-documenting code
    - krandom uses API only

10. **Classpath Scanning** 🏆 (⚠️ but slow)
    - Auto-discover concrete implementations for interfaces/abstract classes
    - Uses ClassGraph library
    - krandom returns null for abstract types

11. **Comprehensive Date/Time Support** 🏆
    - 20+ JSR 310 randomizers (`LocalDate`, `Instant`, `ZonedDateTime`, etc.)
    - Range versions for all types
    - Global date/time range configuration
    - krandom has none

12. **DataFaker Integration** 🏆
    - 20+ realistic randomizers (names, emails, addresses, phones, companies)
    - Locale-aware via DataFaker
    - krandom now has 50+ generators including names, email, phone, address, finance (no DataFaker dep)

13. **Extensible Architecture** 🏆
    - 4 extension interfaces: `RandomizerProvider`, `RandomizerRegistry`, `ExclusionPolicy`, `ObjectFactory`
    - Override any part of generation pipeline
    - krandom has limited extension points

### krandom Strengths (vs Easy Random)

1. **Kotlin-First Design** 🎯
    - Idiomatic Kotlin API
    - Null-safety built-in
    - Extension functions
    - Easy Random is Java-centric

2. **Cleaner Project Structure** 🎯
    - 99%+ test coverage (JaCoCo enforced)
    - Modular architecture (core, java-api, kotlin-api, scala-api)
    - Easy Random is maintenance mode

3. **Simpler API Surface** 🎯
    - Fewer concepts to learn
    - No complex registry/provider/policy abstractions
    - Easy Random has steep learning curve

4. **Deterministic by Default** 🎯
    - `SecureRandom` unless seeded
    - Easy Random defaults to seed 123L (surprising)

5. **Network Generators** 🎯
    - RFC-compliant IPv4/IPv6 generators
    - Easy Random uses DataFaker (less control)

6. **Game Utilities** 🎯
    - Dice (D4-D20) with fairness guarantee, dice sum
    - Coin flip
    - Easy Random has none

7. **Algorithm Generators** 🎯
    - Fibonacci sequence
    - Luhn check digit, ISO 7064 (national IDs)
    - Easy Random has none (beyond Luhn randomizer)

8. **Active Development** 🎯
    - Easy Random is in maintenance mode since 2020
    - krandom adds new generators regularly

9. **Safer Defaults** 🎯
    - Max depth: 5 (vs Integer.MAX_VALUE)
    - Collection size: [1, 10] (vs [1, 100])
    - Prevents accidental resource exhaustion

10. **Functional API** 🎯
    - `Generator<T>` with `map()`, `filter()`, `stream()`
    - Composable generators
    - Easy Random is imperative

11. **Finance Generators** 🎯
    - Credit cards (Luhn-valid, 4 card types), CVV, expiry
    - Money/currency formatting (10 locales)
    - FX currency pairs (44 currencies, locale-aware base)
    - Easy Random has none

12. **10-Country National IDs** 🎯
    - US SSN, UK NI, AU TFN, FR NIR, DE Steuer-ID, JP My Number, ES DNI, IT Codice Fiscale, BR CPF, CN Resident ID
    - Each with proper checksum/check-digit validation
    - Easy Random has none

13. **Locale-Aware Birthday Strings** 🎯
    - `BirthdayGenerator.generateAsString()` uses locale-appropriate date format
    - `de_DE` → `27.5.1983`, `ja_JP` → `1983/5/27`, `zh_CN` → `1983年5月27日`
    - Easy Random has none

---

## COMPATIBILITY ASSESSMENT

### Direct Port Feasibility

| Feature Category                        | Difficulty  | Recommendation                       |
|-----------------------------------------|-------------|--------------------------------------|
| **Object generation (pool, Objenesis)** | ⚠️ MODERATE | Port architecture, significant value |
| **Array/collection support**            | ✅ EASY      | Straightforward, high ROI            |
| **Field exclusion (predicates)**        | ✅ EASY      | Simple, useful                       |
| **@Exclude / @Randomizer annotations**  | ✅ EASY      | High developer experience value      |
| **Context-aware generators**            | ⚠️ MODERATE | Powerful but requires refactoring    |
| **Registry system (layered)**           | ⚠️ HARD     | Complex, questionable ROI            |
| **Bean Validation module**              | ⚠️ MODERATE | Separate module, good ROI            |
| **Date/time randomizers**               | ✅ EASY      | Essential, straightforward           |
| **Realistic data (DataFaker)**          | ⚠️ MODERATE | Add dependency or build own          |
| **Classpath scanning**                  | ❌ SKIP      | Slow, complex, low ROI               |
| **ServiceLoader SPI**                   | ❌ SKIP      | Over-engineering for krandom scale   |

### Recommended Approach

**DO PORT**:

1. ✅ Object pool for circular references (critical safety feature)
2. ✅ Objenesis fallback (broad compatibility)
3. ✅ Array/collection auto-population (complete object graphs)
4. ✅ Field exclusion predicates + `@Exclude` (developer experience)
5. ✅ `@Randomizer` annotation (declarative configuration)
6. ✅ Date/time generators (JSR 310 essential)
7. ✅ Bean Validation support (separate module, high value)
8. ✅ Regex-based generation (flexible)

**DON'T PORT**:

1. ❌ Classpath scanning (slow, edge case)
2. ❌ Full registry system (over-engineered)
3. ❌ ServiceLoader SPI (unnecessary complexity)
4. ❌ Legacy date/time types (`java.util.Date`, etc.) - obsolete
5. ❌ All DataFaker randomizers (dependency bloat) - cherry-pick essentials

**ADAPT**:

1. 🔄 Context-aware generators → simpler field-aware generators
2. 🔄 Predicate composition → Kotlin DSL
3. 🔄 Realistic data → lightweight built-ins, no DataFaker dependency

---

## KEY ARCHITECTURAL LESSONS

### 1. Object Pool Pattern (CRITICAL)

```kotlin
class RandomizationContext(private val config: ObjectGeneratorConfig) {
    private val objectPool = mutableMapOf<Class<*>, ArrayDeque<Any>>()

    fun <T> getOrCreateInstance(clazz: Class<T>, factory: () -> T): T {
        val pool = objectPool.getOrPut(clazz) { ArrayDeque(config.objectPoolSize) }

        // Return cached instance if pool is full (circular ref guard)
        if (pool.size >= config.objectPoolSize) {
            return pool.first() as T
        }

        // Create new instance
        val instance = factory()
        pool.add(instance)
        return instance
    }
}
```

### 2. Field Resolution Order (BEST PRACTICE)

```kotlin
class FieldGeneratorResolver(private val config: ObjectGeneratorConfig) {
    fun resolve(field: Field, context: RandomizationContext): Generator<*>? {
        // 1. Exclusion check
        if (config.shouldExclude(field)) return SkipGenerator

        // 2. @Randomizer annotation
        field.getAnnotation(Randomizer::class.java)?.let {
            return instantiateRandomizer(it)
        }

        // 3. Field-level override
        config.fieldOverrides["${field.declaringClass.name}.${field.name}"]?.let {
            return it
        }

        // 4. Type-level override
        config.typeOverrides[field.type]?.let { return it }

        // 5. Bean Validation constraints
        resolveBeanValidationGenerator(field)?.let { return it }

        // 6. Built-in types (primitives, String, etc.)
        Generators.forType(field.type)?.let { return it }

        // 7. Enums
        if (field.type.isEnum) return EnumGenerator(field.type)

        // 8. Arrays
        if (field.type.isArray) return ArrayGenerator(field.type.componentType)

        // 9. Collections
        if (Collection::class.java.isAssignableFrom(field.type)) {
            return CollectionGenerator(resolveElementType(field.genericType))
        }

        // 10. Depth guard
        if (context.currentDepth >= config.maxDepth) return NullGenerator

        // 11. Recursive nested object
        return ObjectGenerator(field.type, config)
    }
}
```

### 3. Predicate Composition (ELEGANT)

```kotlin
typealias FieldPredicate = (Field) -> Boolean

object FieldPredicates {
    fun named(pattern: String): FieldPredicate =
        { it.name.matches(Regex(pattern)) }

    fun ofType(type: Class<*>): FieldPredicate =
        { it.type == type }

    fun inClass(clazz: Class<*>): FieldPredicate =
        { it.declaringClass == clazz }

    fun annotatedWith(annotation: KClass<out Annotation>): FieldPredicate =
        { it.isAnnotationPresent(annotation.java) }
}

infix fun FieldPredicate.and(other: FieldPredicate): FieldPredicate =
    { this(it) && other(it) }

infix fun FieldPredicate.or(other: FieldPredicate): FieldPredicate =
    { this(it) || other(it) }

operator fun FieldPredicate.not(): FieldPredicate =
    { !this(it) }

// Usage:
val predicate = FieldPredicates.named("password") and
        FieldPredicates.inClass(User::class.java)
```

---

## CONCLUSION

### Completed Implementations ✅

krandom has successfully implemented **17 core features** focusing on statistical generators and primitive types:

**Numbers & Statistical Generators (8/8 complete)**:

- ✅ Natural number generation with exclusion (`NaturalNumberGenerator`)
- ✅ Prime number generation (`PrimeGenerator` - Sieve of Eratosthenes)
- ✅ Fixed decimal precision (`withPrecision()` for Double/Float)
- ✅ Normal distribution (`NormalDistributionGenerator` - Box-Muller transform)
- ✅ Range-based bounded generators (all numeric types)
- ✅ Deterministic seeding (all generators)

**Primitives & Basic Types (9/9 complete)**:

- ✅ All primitive types: boolean, byte, short, int, long, float, double, char
- ✅ Enum randomization with type safety

**Quality Metrics**:

- Test Coverage: 99.7% line, 99.1% branch
- ~300 new comprehensive test cases
- All pre-commit checks passing
- Production-ready implementations

### Focus Areas for krandom

Easy Random's key value propositions are **object graph safety** and **declarative configuration**, not realistic data. krandom should prioritize:

1. **⚡ CRITICAL (Weeks 1-2)**:
    - Circular reference handling (object pool)
    - Array/collection auto-population
    - Email, UUID, regex generators

2. **🎯 HIGH VALUE (Weeks 3-4)**:
    - Field exclusion predicates + `@Exclude`
    - `@Randomizer` annotation
    - Date/time generators (JSR 310)

3. **✅ NICE-TO-HAVE (Weeks 5-6)**:
    - Bean Validation module
    - Context-aware generators
    - Realistic data generators (names, addresses, cards)
    - Objenesis fallback

4. **❌ SKIP**:
    - Classpath scanning (slow, niche)
    - Full registry system (over-engineered)
    - ServiceLoader SPI (unnecessary)
    - Legacy date types (obsolete)
    - DataFaker dependency (bloat)

### Maintain krandom Identity

**DON'T** become an Easy Random clone:

- Keep Kotlin-first design
- Keep functional API (`map`, `filter`, `stream`)
- Keep simpler architecture
- Keep high test coverage standards
- Keep active development

**DO** learn from Easy Random:

- Object pool pattern (safety)
- Objenesis integration (compatibility)
- Bean Validation support (correctness)
- Annotation-based config (developer experience)
- Field exclusion predicates (flexibility)

### Target Outcome

**Progress Tracking:**

- ✅ Statistical generators: 8/8 features complete (100%)
- ✅ Primitive types: 9/9 complete (100%)
- ⏳ Advanced numbers: 0/3 planned (BigDecimal, BigInteger, UUID)
- ⏳ Date/time: 0/15 planned (JSR 310 types)
- ⏳ Realistic data: 0/10 planned (email, URL, etc.)
- ⏳ Object safety: 0/5 planned (circular refs, pools, etc.)

**Overall: 17/60 core features implemented (28%)**

**Match Easy Random** on:

- ✅ Range-based numeric generation (100% complete)
- ✅ Deterministic seeding (100% complete)
- ✅ Primitive type coverage (100% complete)
- ⏳ Object graph safety (0% = pool + depth needed)
- ⏳ Collection support (0%)
- ⏳ Date/time generation (0%)
- ⏳ Declarative configuration (0% = annotations needed)

**Exceed Easy Random** on:

- 🚀 Statistical capabilities (normal distribution, primes - unique to krandom)
- 🚀 API simplicity (Kotlin DSL)
- 🚀 Test coverage (99.7% vs unknown)
- 🚀 Active development (vs maintenance mode)
- 🚀 Modern algorithms (Sieve, Box-Muller)

**Total Effort**: ~35 days (~7 weeks) for comprehensive parity
**Minimal Viable Port**: ~10 days (Phase 1 + critical Phase 5)
**Progress**: 17/60 features = 28% complete
