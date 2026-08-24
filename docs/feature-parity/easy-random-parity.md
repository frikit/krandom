# Easy Random Feature Parity Analysis

> **Current baseline:** The migration and benchmark contract is verified against Easy Random
> 6.0.0. Older feature-count sections below remain historical implementation evidence; use the
> [migration guide](../migration/from-easyrandom.md) and
> [generator catalog](../../docs-site/generator-catalog.md) for current APIs.

## Library Overview

- **Name**: Easy Random
- **Language**: Java
- **Version Analyzed**: 6.0.0 (Java 17+), with historical 5.0.x notes retained below
- **GitHub**: https://github.com/j-easy/easy-random
- **License**: MIT
- **Status**: Maintained 6.x line
- **Key Strength**: Reflection-based object graph population, minimal configuration, ObjectMother pattern implementation

*Last Updated: 2026-08-21*

## Java Execution Plan

- Active plan: `docs/plans/easy-random-java-plan.md`
- Current scope: Java parity only (Kotlin/Scala deferred)
- Delivery model: one parity slice at a time with tests + `./scripts/pre_commit_check.sh`

## Executive Summary

Easy Random is a specialized library focused on **object graph randomization** rather than
realistic data generation. It excels at **populating arbitrary Java objects with random values**
to eliminate hand-crafted test fixtures. It implements the Object Mother pattern for the JVM,
making it useful for:

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

## Java Parity Contract — 2026-04-28

This document treats parity as **100% of krandom's scoped Java object-generation contract**. Core object creation, recursion guards, records, overrides, exclusion predicates, declarative randomizers, Bean Validation basics, Java type coverage, and realistic-data generators are covered. Easy Random implementation details that are intentionally not part of krandom are marked as scope decisions: subclassing `java.util.Random`, ClassGraph-style classpath scanning, ServiceLoader randomizer registries, custom object factories, and dedicated null/skip randomizer classes.

---

## Implementation Status

### Completed Features ✅

**Numbers & Statistical Generators (10 features)**

| Feature                 | krandom Implementation                            | Status |
|-------------------------|---------------------------------------------------|--------|
| Natural numbers (≥0)    | `NaturalNumberGenerator`                          | ✓ DONE |
| Prime number generation | `PrimeGenerator` (Sieve of Eratosthenes)          | ✓ DONE |
| Fixed decimal precision | `DoubleGenerator.withPrecision(decimals)`         | ✓ DONE |
| Fixed decimal precision | `FloatGenerator.withPrecision(decimals)`          | ✓ DONE |
| Normal distribution     | `NormalDistributionGenerator` (Box-Muller)        | ✓ DONE |
| Value exclusion         | `NaturalNumberGenerator.excluding(...)`           | ✓ DONE |
| Range-based generation  | All bounded generators (Int, Long, Double, etc.)  | ✓ DONE |
| Deterministic seeding   | All generators support seed parameter             | ✓ DONE |
| BigDecimal              | `BigDecimalGenerator` (range + scale, seeded)     | ✓ DONE |
| BigInteger              | `BigIntegerGenerator` (range, rejection sampling) | ✓ DONE |

**Primitives & Basic Types (10 features)**

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
| Regex-based string | `RegexGenerator`       | ✓ DONE |

**Object Generation (implemented)**

| Feature                    | krandom Implementation                                            | Status |
|----------------------------|-------------------------------------------------------------------|--------|
| Random POJO                | `ObjectGenerator<T>` (reflection, inherited fields)               | ✓ DONE |
| Objenesis fallback         | `ObjenesisStd` bypass when no no-arg constructor                  | ✓ DONE |
| Java Records               | Canonical constructor via record components                       | ✓ DONE |
| Circular reference guard   | `ObjectPool` — detects in-progress types, breaks cycles           | ✓ DONE |
| Max depth control          | `ObjectGeneratorConfig.maxDepth()`                                | ✓ DONE |
| Ignore errors              | `ObjectGeneratorConfig.ignoreErrors()`                            | ✓ DONE |
| Field / type overrides     | `ObjectGeneratorConfig.override(type/field, generator)`           | ✓ DONE |
| Contextual overrides       | `ObjectGeneratorConfig.override(type/field, ContextualGenerator)` | ✓ DONE |
| Array auto-population      | `T[]` populated with `DEFAULT_ELEMENT_COUNT` elements             | ✓ DONE |
| List / Set auto-population | `List<T>`, `Set<T>` populated from element type                   | ✓ DONE |
| Map auto-population        | `Map<K,V>` populated with key-value pairs                         | ✓ DONE |

**Bean Validation Constraint Respect (implemented)**

| Constraint                      | krandom Implementation                             | Status |
|---------------------------------|----------------------------------------------------|--------|
| `@Size(min, max)` on String     | `StringGenerator.letters(min, max)`                | ✓ DONE |
| `@Min` / `@Max` on int/Integer  | `IntGenerator(min, max)` — sign variants included  | ✓ DONE |
| `@Min` / `@Max` on long/Long    | `LongGenerator(min, max)` — sign variants included | ✓ DONE |
| `@Positive` / `@PositiveOrZero` | Bounded IntGenerator / LongGenerator               | ✓ DONE |
| `@Negative` / `@NegativeOrZero` | Bounded IntGenerator / LongGenerator               | ✓ DONE |
| `@DecimalMin` / `@DecimalMax`   | `BigDecimalGenerator(min, max)`                    | ✓ DONE |
| `@Email`                        | `RegexGenerator("[a-z]{4,8}@[a-z]{3,8}\\.(com      | net    |org)")` | ✓ DONE |
| `@Pattern(regexp)`              | `RegexGenerator(regexp)`                           | ✓ DONE |

**Date/Time Generators (implemented)**

| Feature           | krandom Implementation                                     | Status |
|-------------------|------------------------------------------------------------|--------|
| LocalDate         | `DateGenerator` (1970–2100, seeded, bounded constructor)   | ✓ DONE |
| LocalTime         | `TimeGenerator`                                            | ✓ DONE |
| LocalDateTime     | `LocalDateTimeGenerator` (1970–2100, seeded, bounded)      | ✓ DONE |
| Instant           | `InstantGenerator` (1970–2100, UTC midnight, bounded)      | ✓ DONE |
| ZonedDateTime     | `ZonedDateTimeGenerator` (1970–2100, random zone, bounded) | ✓ DONE |
| Global date range | `ObjectGeneratorConfig.dateRange(min, max)`                | ✓ DONE |

**Realistic & Finance Generators (implemented)**

| Feature               | krandom Implementation                                         | Status |
|-----------------------|----------------------------------------------------------------|--------|
| First name            | `FirstNameGenerator` (10 locales, gender-aware)                | ✓ DONE |
| Last name             | `LastNameGenerator` (10 locales)                               | ✓ DONE |
| Full name             | `FullNameGenerator` (10 locales, gender-aware)                 | ✓ DONE |
| Email                 | `EmailGenerator` (7 formats, locale-aware)                     | ✓ DONE |
| Phone number          | `PhoneNumberGenerator` (10 locales, mobile support)            | ✓ DONE |
| City                  | `CityGenerator` (10 locales)                                   | ✓ DONE |
| State / Province      | `StateGenerator` (10 locales, full + abbrev)                   | ✓ DONE |
| Country               | `CountryGenerator` (10 locales)                                | ✓ DONE |
| ZIP / Postal code     | `PostalCodeGenerator` (10 locales)                             | ✓ DONE |
| Street address        | `StreetAddressGenerator` (number + name + type)                | ✓ DONE |
| Latitude              | `CoordinateGenerator.latitude()`                               | ✓ DONE |
| Longitude             | `CoordinateGenerator.longitude()`                              | ✓ DONE |
| Credit card           | `CreditCardGenerator` (Visa/MC/Amex/Discover, Luhn-valid)      | ✓ DONE |
| UUID v4               | `UUIDGenerator`                                                | ✓ DONE |
| URL                   | `UrlGenerator` (http/https, path, query)                       | ✓ DONE |
| Money / currency      | `MoneyGenerator` (10 locales, locale-aware formatting)         | ✓ DONE |
| Currency pair         | `CurrencyPairGenerator` (44 currencies, locale base)           | ✓ DONE |
| National ID           | `NationalIdGenerator` (10 countries: SSN, NI, TFN, NIR, etc.)  | ✓ DONE |
| Birthday              | `BirthdayGenerator` (age ranges, locale-aware string output)   | ✓ DONE |
| Color (hex)           | `HexColorGenerator`                                            | ✓ DONE |
| IPv4 address          | `IPv4Generator` (RFC-compliant)                                | ✓ DONE |
| IPv6 address          | `IPv6Generator` (RFC-compliant)                                | ✓ DONE |
| MAC address           | `MacAddressGenerator` (upper/lower, colon/dash separator)      | ✓ DONE |
| Company name          | `CompanyNameGenerator` (prefix + noun + optional suffix)       | ✓ DONE |
| ISBN                  | `IsbnGenerator` (ISBN-10 / ISBN-13, valid check digit)         | ✓ DONE |
| Lorem ipsum word      | `LoremIpsumGenerator(Mode.WORD)`                               | ✓ DONE |
| Lorem ipsum sentence  | `LoremIpsumGenerator(Mode.SENTENCE)` / `generateSentence(n)`   | ✓ DONE |
| Lorem ipsum paragraph | `LoremIpsumGenerator(Mode.PARAGRAPH)` / `generateParagraph(n)` | ✓ DONE |

**Context-Aware Generators (implemented)**

| Feature                  | krandom Implementation                                          | Status |
|--------------------------|-----------------------------------------------------------------|--------|
| `GenerationContext`      | `fieldName`, `ownerType`, `depth` available per-call            | ✓ DONE |
| `ContextualGenerator<T>` | `@FunctionalInterface ctx -> T`                                 | ✓ DONE |
| Per-type contextual      | `config.override(String.class, ctx -> ctx.getFieldName()+"_v")` | ✓ DONE |
| Per-field contextual     | `config.override(Owner.class, "field", ctx -> ...)`             | ✓ DONE |

**Implementation Metrics:**

- **Total Features Implemented**: 70+ features across all categories
- **Test Coverage**: 99.8% line, 99.6% branch
- **Algorithms**: Sieve of Eratosthenes (primes), Box-Muller (normal dist.), Luhn (cards), ISO 7064 (national IDs), ISBN-10/13 check digits
- **Pre-commit Checks**: ALL PASSING ✅

### Remaining deliberate differences

- Classpath scanning for abstract/interface types is intentionally replaced by explicit subtype
  mappings.
- ServiceLoader randomizer discovery is intentionally replaced by explicit provider/configuration
  registration.
- Exact seeded values and setter side effects are not migration guarantees.
- Java 17 consumers must move to Java 21 to adopt krandom 2.x.

---

## Feature Categories

### 1. OBJECT GENERATION (Core Capability)

| Feature                     | Easy Random Support                      | krandom Status | Implementation Priority | Notes                                                             |
|-----------------------------|------------------------------------------|----------------|-------------------------|-------------------------------------------------------------------|
| **Basic Object Creation**   |
| Generate random POJO        | ✅ `nextObject(Class<T>)`                 | ✅ Yes          | ✓ DONE                  | krandom has ObjectGenerator<T>                                    |
| Generate object stream      | ✅ `objects(Class<T>, int)`               | ✅ Yes          | ✓ DONE                  | krandom uses `Generator.stream()`                                 |
| Extend java.util.Random     | ✅ Yes                                    | No (intentional) | SKIP                | krandom composes generators instead of subclassing `Random`       |
| Deterministic seed          | ✅ Constructor/config                     | ✅ Yes          | ✓ DONE                  | Both support seeded generation                                    |
| **Object Instantiation**    |
| No-arg constructor          | ✅ Yes                                    | ✅ Yes          | ✓ DONE                  | Both require no-arg constructor                                   |
| Objenesis fallback          | ✅ Yes                                    | ✅ Yes          | ✓ DONE                  | krandom: ObjenesisStd fallback                                    |
| Java Records                | ✅ Canonical constructor                  | ✅ Yes          | ✓ DONE                  | Both support records                                              |
| Abstract/interface types    | ✅ With classpath scanning                | No (intentional) | SKIP                | Runtime classpath scanning is intentionally avoided               |
| Custom ObjectFactory        | ✅ `objectFactory(factory)`               | No (intentional) | SKIP                | Objenesis + constructors cover the current scope; factory SPI is not adopted |
| **Field Population**        |
| Declared fields             | ✅ Yes                                    | ✅ Yes          | ✓ DONE                  | Instance fields only                                              |
| Inherited fields            | ✅ Full hierarchy                         | ✅ Yes          | ✓ DONE                  | Both walk class hierarchy                                         |
| Static fields               | ✅ Skipped                                | ✅ Skipped      | ✓ DONE                  | Both skip static                                                  |
| Final fields                | ✅ Yes (reflection)                       | ✅ Yes          | ✓ DONE                  |                                                                   |
| Transient fields            | ✅ Yes                                    | ✅ Yes          | ✓ DONE                  | Populated by default                                              |
| Override existing values    | ✅ `overrideDefaultInitialization(true)`  | ✅ Yes          | ✓ DONE                  | Preserves non-default initial values by default; opt-in overwrite |
| Bypass setters              | ✅ `bypassSetters(true)`                  | ✅ Yes          | ✓ DONE                  | Direct field access                                               |
| **Nested Objects**          |
| Recursive population        | ✅ Yes                                    | ✅ Yes          | ✓ DONE                  | Both support                                                      |
| Max depth control           | ✅ `randomizationDepth(int)`              | ✅ Yes          | ✓ DONE                  | krandom: maxDepth                                                 |
| Circular reference handling | ✅ Object pool                            | ✅ Yes          | ✓ DONE                  | krandom: ObjectPool cycle guard                                   |
| Object pool size            | ✅ `objectPoolSize(int)`                  | ✅ Yes          | ✓ DONE                  | Configurable bounded per-type cache                               |
| **Generics Support**        |
| Simple generics             | ✅ `List<String>`                         | ✅ Yes          | ✓ DONE                  | Object generation supports common typed collections               |
| Nested generics             | ⚠️ Limited `List<List<T>>`               | No (intentional) | SKIP                | JVM type-erasure edge case, not core scope                        |
| Generic inheritance         | ✅ `StringList extends ArrayList<String>` | No (intentional) | SKIP                | Rare inheritance shape; explicit generator override is preferred  |

### 2. EXCLUSION & FILTERING

| Feature                   | Easy Random Support                          | krandom Status | Implementation Priority | Notes                                                                                     |
|---------------------------|----------------------------------------------|----------------|-------------------------|-------------------------------------------------------------------------------------------|
| **Field Exclusion**       |
| Exclude by annotation     | ✅ `@Exclude`                                 | ✅ Yes          | ✓ DONE                  | Declarative exclusion                                                                     |
| Exclude by name           | ✅ `FieldPredicates.named("password")`        | ✅ Yes          | ✓ DONE                  | Exact-name matching                                                                       |
| Exclude by type           | ✅ `FieldPredicates.ofType(Class)`            | ✅ Yes          | ✓ DONE                  | Type-based filtering                                                                      |
| Exclude by class          | ✅ `FieldPredicates.inClass(Class)`           | ✅ Yes          | ✓ DONE                  | Scope to specific class                                                                   |
| Exclude by annotation     | ✅ `FieldPredicates.isAnnotatedWith()`        | ✅ Yes          | ✓ DONE                  | Match annotated fields                                                                    |
| Exclude by modifiers      | ✅ `FieldPredicates.hasModifiers(int)`        | ✅ Yes          | ✓ DONE                  | Access-level filtering                                                                    |
| Exclude entire types      | ✅ `TypePredicates.inPackage("com.internal")` | ✅ Yes          | ✓ DONE                  | Package-based type exclusion via `excludeType(TypePredicates.inPackage(...))` |
| Custom ExclusionPolicy    | ✅ `exclusionPolicy(policy)`                  | No (intentional) | SKIP                | Predicate-based exclusions are the supported extension point      |
| **Predicate Composition** |
| AND logic                 | ✅ `predicate1.and(predicate2)`               | ✅ Yes          | ✓ DONE                  | Combine predicates                                                                        |
| OR logic                  | ✅ `predicate1.or(predicate2)`                | ✅ Yes          | ✓ DONE                  |                                                                                           |
| NOT logic                 | ✅ `predicate.negate()`                       | ✅ Yes          | ✓ DONE                  |                                                                                           |

### 3. CUSTOM RANDOMIZERS

| Feature                | Easy Random Support                                   | krandom Status | Implementation Priority | Notes                                     |
|------------------------|-------------------------------------------------------|----------------|-------------------------|-------------------------------------------|
| **Randomizer API**     |
| Functional interface   | ✅ `Randomizer<T>`                                     | ✅ Yes          | ✓ DONE                  | krandom: Generator<T>                     |
| Lambda support         | ✅ `() -> "value"`                                     | ✅ Yes          | ✓ DONE                  | Both functional                           |
| Context-aware          | ✅ `ContextAwareRandomizer<T>`                         | ✅ Yes          | ✓ DONE                  | krandom: ContextualGenerator<T>           |
| RandomizerContext      | ✅ Target type, root object, current field path, depth | ✅ Yes          | ✓ DONE                  | krandom exposes fieldName, ownerType, and depth |
| **Registration**       |
| Type-level randomizer  | ✅ `randomize(String.class, randomizer)`               | ✅ Yes          | ✓ DONE                  | krandom: typeOverrides                    |
| Field-level randomizer | ✅ `randomize(predicate, randomizer)`                  | ✅ Yes          | ✓ DONE                  | krandom: fieldOverrides                   |
| Annotation-based       | ✅ `@Randomizer(EmailRandomizer.class)`                | ✅ Yes          | ✓ DONE                  | Declarative field/component randomizer    |
| Randomizer arguments   | ✅ `@RandomizerArgument`                               | ✅ Yes          | ✓ DONE                  | Declarative constructor parameter binding |
| **Registry System**    |
| RandomizerRegistry     | ✅ Interface + SPI discovery                           | No (intentional) | SKIP                | Registry SPI/priority model is intentionally not adopted |
| Registry priority      | ✅ `@Priority` annotation                              | No (intentional) | SKIP                | Explicit overrides provide deterministic precedence |
| Built-in registries    | ✅ 6 registries (Internal, Time, BeanValidation, etc.) | No (intentional) | SKIP                | krandom uses a direct resolver chain instead of layered registries |
| Custom registries      | ✅ ServiceLoader auto-discovery                        | No (intentional) | SKIP                | ServiceLoader auto-discovery is intentionally avoided |
| RandomizerProvider     | ✅ Custom provider strategy                            | No (intentional) | SKIP                | Custom generators and contextual overrides are the supported path |

### 4. BEAN VALIDATION INTEGRATION

| Feature                    | Easy Random Support                 | krandom Status | Implementation Priority | Notes                            |
|----------------------------|-------------------------------------|----------------|-------------------------|----------------------------------|
| **JSR 380 Constraints**    |
| @NotNull                   | ✅ Never null                        | ✅ Yes          | ✓ DONE                  | Object generation emits non-null values by default |
| @NotEmpty                  | ✅ Never empty string/collection     | ✅ Yes          | ✓ DONE                  | Default string/collection bounds are non-empty |
| @Size(min, max)            | ✅ Respected for strings/collections | ✅ Yes          | ✓ DONE                  | StringGenerator.letters(min,max) |
| @Min / @Max                | ✅ Numeric bounds                    | ✅ Yes          | ✓ DONE                  | IntGenerator / LongGenerator     |
| @Past / @Future            | ✅ Date constraints                  | No (intentional) | SKIP                | Date ranges are configured globally; annotation-specific temporal bounds are not adopted |
| @Positive / @Negative      | ✅ Sign constraints                  | ✅ Yes          | ✓ DONE                  | Bounded Int/Long generators      |
| @DecimalMin / @DecimalMax  | ✅ Decimal bounds                    | ✅ Yes          | ✓ DONE                  | BigDecimalGenerator(min,max)     |
| @Email                     | ✅ Valid email format                | ✅ Yes          | ✓ DONE                  | RegexGenerator pattern           |
| @Pattern                   | ✅ Regex-based generation            | ✅ Yes          | ✓ DONE                  | RegexGenerator(regexp)           |
| **Constraint Priority**    |
| Override global config     | ✅ Yes                               | ✅ Yes          | ✓ DONE                  | BV runs before built-in fallback |
| Custom randomizer override | ✅ Yes                               | ✅ Yes          | ✓ DONE                  | Custom > BV > built-in           |
| **Module**                 |
| Separate module            | ✅ `easy-random-bean-validation`     | ✅ Inline       | ✓ DONE                  | BeanValidationSupport (same jar) |
| BeanValidation registry    | ✅ Priority -2                       | ✅ Inline       | ✓ DONE                  | FieldGeneratorResolver step 3b   |

### 5. CONFIGURATION PARAMETERS

| Feature               | Easy Random Support                            | krandom Status | Implementation Priority | Notes                                         |
|-----------------------|------------------------------------------------|----------------|-------------------------|-----------------------------------------------|
| **Global Settings**   |
| Seed                  | ✅ `seed(long)` default: 123L                   | ✅ Yes          | ✓ DONE                  | krandom: GeneratorConfig.seed()               |
| Charset               | ✅ `charset(Charset)` default: US_ASCII         | ✅ Yes          | ✓ DONE                  | krandom: GeneratorConfig.charset()            |
| String length range   | ✅ `stringLengthRange(min, max)` [1, 32]        | ✅ Yes          | ✓ DONE                  | krandom: GeneratorConfig.stringLength()       |
| Collection size range | ✅ `collectionSizeRange(min, max)` [1, 100]     | ✅ Yes          | ✓ DONE                  | krandom: GeneratorConfig.collectionSize()     |
| Randomization depth   | ✅ `randomizationDepth(int)` default: MAX_VALUE | ✅ Yes          | ✓ DONE                  | krandom: ObjectGeneratorConfig.maxDepth()     |
| Object pool size      | ✅ `objectPoolSize(int)` default: 10            | ✅ Yes          | ✓ DONE                  | Recursion guard with configurable cache limit |
| **Date/Time Ranges**  |
| Date range            | ✅ `dateRange(LocalDate, LocalDate)`            | ✅ Yes          | ✓ DONE                  | krandom: ObjectGeneratorConfig.dateRange()    |
| Time range            | ✅ `timeRange(LocalTime, LocalTime)`            | No (intentional) | SKIP                | Date range is supported globally; time-only range config is deferred until needed |
| **Behavioral Flags**  |
| Override defaults     | ✅ `overrideDefaultInitialization(bool)`        | ✅ Yes          | ✓ DONE                  | Re-randomize initialized fields               |
| Ignore errors         | ✅ `ignoreRandomizationErrors(bool)`            | ✅ Yes          | ✓ DONE                  | krandom: ObjectGeneratorConfig.ignoreErrors() |
| Bypass setters        | ✅ `bypassSetters(bool)`                        | ✅ Yes          | ✓ DONE                  | Direct field access                           |
| Scan classpath        | ✅ `scanClasspathForConcreteTypes(bool)`        | No (intentional) | SKIP                | Runtime classpath scanning is intentionally avoided |
| **Builder Pattern**   |
| Fluent API            | ✅ All setters return `this`                    | ✅ Yes          | ✓ DONE                  | Both use builders                             |

### 6. BUILT-IN RANDOMIZERS

| Feature                           | Easy Random Support                              | krandom Status | Implementation Priority | Notes                                            |
|-----------------------------------|--------------------------------------------------|----------------|-------------------------|--------------------------------------------------|
| **Primitive & Boxed Types**       |
| boolean / Boolean                 | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  |                                                  |
| byte / Byte                       | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  |                                                  |
| short / Short                     | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  |                                                  |
| int / Integer                     | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  |                                                  |
| long / Long                       | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  |                                                  |
| float / Float                     | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  |                                                  |
| double / Double                   | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  |                                                  |
| char / Character                  | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  |                                                  |
| **Numeric Types**                 |
| BigInteger                        | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  | BigIntegerGenerator                              |
| BigDecimal                        | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  | BigDecimalGenerator (scale 2)                    |
| AtomicInteger                     | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  | Concurrent types                                 |
| AtomicLong                        | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  |                                                  |
| **Range Randomizers**             |
| ByteRangeRandomizer               | ✅ `(min, max)`                                   | ✅ Yes          | ✓ DONE                  | krandom: BoundedGenerator                        |
| ShortRangeRandomizer              | ✅ `(min, max)`                                   | ✅ Yes          | ✓ DONE                  |                                                  |
| IntegerRangeRandomizer            | ✅ `(min, max)`                                   | ✅ Yes          | ✓ DONE                  |                                                  |
| LongRangeRandomizer               | ✅ `(min, max)`                                   | ✅ Yes          | ✓ DONE                  |                                                  |
| FloatRangeRandomizer              | ✅ `(min, max)`                                   | ✅ Yes          | ✓ DONE                  |                                                  |
| DoubleRangeRandomizer             | ✅ `(min, max)`                                   | ✅ Yes          | ✓ DONE                  |                                                  |
| BigDecimalRangeRandomizer         | ✅ `(min, max)`                                   | ✅ Yes          | ✓ DONE                  | BigDecimalGenerator(min,max)                     |
| BigIntegerRangeRandomizer         | ✅ `(min, max)`                                   | ✅ Yes          | ✓ DONE                  | BigIntegerGenerator(min,max)                     |
| **String Types**                  |
| String                            | ✅ Random ASCII                                   | ✅ Yes          | ✓ DONE                  |                                                  |
| StringRandomizer                  | ✅ Custom length                                  | ✅ Yes          | ✓ DONE                  |                                                  |
| GenericStringRandomizer           | ✅ DataFaker-backed                               | No (intentional) | SKIP                | krandom uses its own string/text generators, no DataFaker dependency |
| RegularExpressionRandomizer       | ✅ Regex-based                                    | ✅ Yes          | ✓ DONE                  | RegexGenerator                                   |
| **Standard Library Types**        |
| UUID                              | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  | krandom: UUIDGenerator                           |
| Locale                            | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  | `RandomLocaleGenerator`, `Generators.ofLocale()`, and `forType(Locale.class)` |
| URI                               | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  | `UriGenerator` produces URI strings              |
| URL                               | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  | krandom: UrlGenerator                            |
| **Date/Time (Legacy)**            |
| java.util.Date                    | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  | Legacy support                                   |
| java.util.Calendar                | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  | `CalendarGenerator`, `Generators.ofCalendar()`, and `forType(Calendar.class)` |
| java.util.GregorianCalendar       | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  | `CalendarGenerator` returns non-lenient `GregorianCalendar` values |
| java.sql.Date                     | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  | SQL types                                        |
| java.sql.Time                     | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  |                                                  |
| java.sql.Timestamp                | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  |                                                  |
| java.util.TimeZone                | ✅ Yes                                            | No (intentional) | SKIP                | Timezone IDs and offsets are exposed as strings/`ZoneId` values |
| **Date/Time (JSR 310)**           |
| Instant                           | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  | InstantGenerator                                 |
| LocalDate                         | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  | DateGenerator                                    |
| LocalTime                         | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  | TimeGenerator                                    |
| LocalDateTime                     | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  | LocalDateTimeGenerator                           |
| OffsetDateTime                    | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  | With timezone offset                             |
| OffsetTime                        | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  |                                                  |
| ZonedDateTime                     | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  | ZonedDateTimeGenerator                           |
| Year                              | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  |                                                  |
| YearMonth                         | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  |                                                  |
| MonthDay                          | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  |                                                  |
| Duration                          | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  | Time spans                                       |
| Period                            | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  | Date periods                                     |
| ZoneId                            | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  | Timezone IDs                                     |
| ZoneOffset                        | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  |                                                  |
| **Range Randomizers (Date/Time)** |
| DateRangeRandomizer               | ✅ `(min, max)`                                   | ✅ Yes          | ✓ DONE                  | Legacy date range support via `UtilDateGenerator(min, max)` |
| LocalDateRangeRandomizer          | ✅ `(min, max)`                                   | ✅ Yes          | ✓ DONE                  | DateGenerator(min,max)                           |
| LocalDateTimeRangeRandomizer      | ✅ `(min, max)`                                   | ✅ Yes          | ✓ DONE                  | LocalDateTimeGenerator(min,max)                  |
| LocalTimeRangeRandomizer          | ✅ `(min, max)`                                   | No (intentional) | SKIP                | Time-only range config is deferred until needed |
| InstantRangeRandomizer            | ✅ `(min, max)`                                   | ✅ Yes          | ✓ DONE                  | InstantGenerator(min,max)                        |
| OffsetDateTimeRangeRandomizer     | ✅ `(min, max)`                                   | No (intentional) | SKIP                | Date-bounded generation is available; offset-specific range type is not exposed |
| ZonedDateTimeRangeRandomizer      | ✅ `(min, max)`                                   | ✅ Yes          | ✓ DONE                  | ZonedDateTimeGenerator(min,max)                  |
| YearRangeRandomizer               | ✅ `(min, max)`                                   | ✅ Yes          | ✓ DONE                  | Year generation respects configured date bounds |
| **Enums**                         |
| Enum randomization                | ✅ Random constant                                | ✅ Yes          | ✓ DONE                  | krandom: EnumGenerator                           |
| EnumRandomizer<T>                 | ✅ Generic                                        | ✅ Yes          | ✓ DONE                  |                                                  |
| **Arrays**                        |
| Array population                  | ✅ Within collectionSizeRange                     | ✅ Yes          | ✓ DONE                  | krandom: DEFAULT_ELEMENT_COUNT                   |
| **Collections (JCF)**             |
| List / ArrayList                  | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  | Unmodifiable list                                |
| LinkedList                        | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  |                                                  |
| Set / HashSet                     | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  | LinkedHashSet                                    |
| LinkedHashSet                     | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  |                                                  |
| TreeSet                           | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  |                                                  |
| Queue / ArrayDeque                | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  |                                                  |
| PriorityQueue                     | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  |                                                  |
| Collection randomizers            | ✅ ListRandomizer, SetRandomizer, QueueRandomizer | ✅ Yes          | ✓ DONE                  | Object generation populates common collection types |
| EnumSet                           | ✅ EnumSetRandomizer                              | No (intentional) | SKIP                | Rare specialized collection; use custom generator override |
| **Maps**                          |
| Map / HashMap                     | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  | LinkedHashMap, unmodifiable                      |
| LinkedHashMap                     | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  |                                                  |
| TreeMap                           | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  |                                                  |
| Hashtable                         | ✅ Yes                                            | No (intentional) | SKIP                | Legacy synchronized map type |
| WeakHashMap                       | ✅ Yes                                            | No (intentional) | SKIP                | Rare specialized map; use custom generator override |
| IdentityHashMap                   | ✅ Yes                                            | No (intentional) | SKIP                | Rare specialized map; use custom generator override |
| EnumMap                           | ✅ Yes                                            | No (intentional) | SKIP                | Rare specialized map; use custom generator override |
| MapRandomizer                     | ✅ `MapRandomizer<K,V>`                           | ✅ Yes          | ✓ DONE                  | Map population is built into object generation |
| **Optional**                      |
| Optional<T>                       | ✅ OptionalPopulator                              | ✅ Yes          | ✓ DONE                  | Java 8 optionals via resolver                    |
| OptionalRandomizer                | ✅ Yes                                            | ✅ Yes          | ✓ DONE                  | Behavior covered in resolver |
| **Utility Randomizers**           |
| ConstantRandomizer                | ✅ Always same value                              | ✅ Yes          | ✓ DONE                  | `ConstantGenerator<T>`, `Generators.ofConstant(value)`, and `Generators.ofConstant(value)` |
| NullRandomizer                    | ✅ Always null                                    | No (intentional) | SKIP                | Use constant/null custom generator where needed |
| SkipRandomizer                    | ✅ Leave field unset                              | No (intentional) | SKIP                | Use exclusion predicates for skip semantics |

### 7. REALISTIC DATA (DataFaker Integration)

| Feature           | Easy Random Support            | krandom Status | Implementation Priority | Notes                                             |
|-------------------|--------------------------------|----------------|-------------------------|---------------------------------------------------|
| **Personal Data** |
| First name        | ✅ `FirstNameRandomizer`        | ✅ Yes          | ✓ DONE                  | krandom: FirstNameGenerator (10 locales)          |
| Last name         | ✅ `LastNameRandomizer`         | ✅ Yes          | ✓ DONE                  | krandom: LastNameGenerator (10 locales)           |
| Full name         | ✅ `FullNameRandomizer`         | ✅ Yes          | ✓ DONE                  | krandom: FullNameGenerator (10 locales)           |
| Email             | ✅ `EmailRandomizer`            | ✅ Yes          | ✓ DONE                  | krandom: EmailGenerator (7 formats)               |
| Phone number      | ✅ `PhoneNumberRandomizer`      | ✅ Yes          | ✓ DONE                  | krandom: PhoneNumberGenerator (10 locales)        |
| Password          | ✅ `PasswordRandomizer`         | ✅ Yes          | ✓ DONE                  | `PasswordGenerator`                               |
| **Address Data**  |
| Street            | ✅ `StreetRandomizer`           | ✅ Yes          | ✓ DONE                  | krandom: StreetAddressGenerator                   |
| City              | ✅ `CityRandomizer`             | ✅ Yes          | ✓ DONE                  | krandom: CityGenerator (10 locales)               |
| State             | ✅ `StateRandomizer`            | ✅ Yes          | ✓ DONE                  | krandom: StateGenerator (10 locales, full+abbrev) |
| Country           | ✅ `CountryRandomizer`          | ✅ Yes          | ✓ DONE                  | krandom: CountryGenerator (10 locales)            |
| ZIP code          | ✅ `ZipCodeRandomizer`          | ✅ Yes          | ✓ DONE                  | krandom: PostalCodeGenerator (10 locales)         |
| Latitude          | ✅ `LatitudeRandomizer`         | ✅ Yes          | ✓ DONE                  | krandom: CoordinateGenerator.latitude()           |
| Longitude         | ✅ `LongitudeRandomizer`        | ✅ Yes          | ✓ DONE                  | krandom: CoordinateGenerator.longitude()          |
| **Company Data**  |
| Company name      | ✅ `CompanyRandomizer`          | ✅ Yes          | ✓ DONE                  | krandom: CompanyNameGenerator                     |
| **Text Data**     |
| Paragraph         | ✅ `ParagraphRandomizer`        | ✅ Yes          | ✓ DONE                  | LoremIpsumGenerator(Mode.PARAGRAPH)               |
| Sentence          | ✅ `SentenceRandomizer`         | ✅ Yes          | ✓ DONE                  | LoremIpsumGenerator(Mode.SENTENCE)                |
| Word              | ✅ `WordRandomizer`             | ✅ Yes          | ✓ DONE                  | LoremIpsumGenerator(Mode.WORD)                    |
| **Network Data**  |
| IPv4 address      | ✅ `Ipv4AddressRandomizer`      | ✅ Yes          | ✓ DONE                  | krandom: IPv4Generator                            |
| IPv6 address      | ✅ `Ipv6AddressRandomizer`      | ✅ Yes          | ✓ DONE                  | krandom: IPv6Generator                            |
| MAC address       | ✅ `MacAddressRandomizer`       | ✅ Yes          | ✓ DONE                  | krandom: MacAddressGenerator                      |
| **Product Data**  |
| ISBN              | ✅ `IsbnRandomizer`             | ✅ Yes          | ✓ DONE                  | IsbnGenerator (ISBN-10 / ISBN-13)                 |
| Credit card       | ✅ `CreditCardNumberRandomizer` | ✅ Yes          | ✓ DONE                  | krandom: CreditCardGenerator (Luhn-valid)         |

### 8. CLASSPATH SCANNING

| Feature                      | Easy Random Support                     | krandom Status | Implementation Priority | Notes                   |
|------------------------------|-----------------------------------------|----------------|-------------------------|-------------------------|
| Abstract class instantiation | ✅ Random concrete subtype               | No (intentional) | SKIP                | Requires classpath scanning, intentionally avoided |
| Interface instantiation      | ✅ Random implementation                 | No (intentional) | SKIP                | Requires classpath scanning, intentionally avoided |
| Enable scanning              | ✅ `scanClasspathForConcreteTypes(true)` | No (intentional) | SKIP                | Uses ClassGraph-style behavior, intentionally avoided |
| Performance                  | ⚠️ Slow on large classpaths             | No (intentional) | SKIP                | Avoiding runtime scanning is the performance guardrail |

### 9. ADVANCED FEATURES

| Feature                        | Easy Random Support                   | krandom Status        | Implementation Priority | Notes                       |
|--------------------------------|---------------------------------------|-----------------------|-------------------------|-----------------------------|
| **Recursion Control**          |
| Circular ref detection         | ✅ Object pool caching                 | ✅ ObjectPool          | ✓ DONE                  | Prevent stack overflow      |
| Max depth guard                | ✅ `randomizationDepth(int)`           | ✅ Yes                 | ✓ DONE                  | Both support                |
| Pool size config               | ✅ `objectPoolSize(int)`               | ✅ Yes                 | ✓ DONE                  | Cache size control          |
| **Error Handling**             |
| Ignore errors                  | ✅ `ignoreRandomizationErrors(true)`   | ✅ Yes                 | ✓ DONE                  | Silently set null           |
| Throw exceptions               | ✅ `ignoreRandomizationErrors(false)`  | ✅ Default             | ✓ DONE                  | Fail fast                   |
| **Inner Classes**              |
| Static nested classes          | ✅ Yes                                 | ✅ Yes                 | ✓ DONE                  | Fully supported             |
| Non-static inner classes       | ⚠️ Cannot instantiate                 | ⚠️ Cannot instantiate | N/A                     | Requires enclosing instance |
| **Extension Points**           |
| RandomizerProvider             | ✅ Custom resolution strategy          | No (intentional) | SKIP                    | Explicit overrides are the supported extension point |
| RandomizerRegistry             | ✅ Group randomizers                   | No (intentional) | SKIP                    | Registry SPI is intentionally not adopted |
| ExclusionPolicy                | ✅ Custom exclusion logic              | No (intentional) | SKIP                    | Predicate-based exclusions are the supported API |
| ObjectFactory                  | ✅ Custom instantiation                | No (intentional) | SKIP                    | Objenesis + constructors cover current scope |
| **Service Provider Interface** |
| ServiceLoader discovery        | ✅ `META-INF/services/`                | No (intentional) | SKIP                    | Auto-discovery is intentionally avoided |
| **Known Limitations**          |
| Type erasure                   | ⚠️ Limited nested generics            | ⚠️ Same issue         | N/A                     | JVM limitation              |
| Android support                | ❌ Objenesis/ClassGraph not compatible | N/A                   | N/A                     | Desktop JVM only            |
| javax.xml.datatype             | Not supported                       | Not supported       | LOW                     | XMLGregorianCalendar        |
| @Digits constraint             | Not in BeanValidation module        | No (intentional) | SKIP                    | Not part of Easy Random Bean Validation scope |

---

## ADVANCED FEATURES COMPARISON

### Object Generation Architecture

| Aspect              | Easy Random                          | krandom                  | Winner      |
|---------------------|--------------------------------------|--------------------------|-------------|
| **Instantiation**   | Objenesis (no-arg not required)      | Objenesis fallback       | Tie         |
| **Recursion guard** | Object pool (configurable size)      | ObjectPool + depth limit | Tie         |
| **Circular refs**   | Handled via pool                     | Handled via ObjectPool   | Tie         |
| **Inheritance**     | Full hierarchy walked                | Full hierarchy walked    | Tie         |
| **Field access**    | Direct reflection + optional setters | Direct reflection        | Tie         |
| **Generic types**   | Limited nested generics              | Basic support            | Tie         |
| **Records**         | Canonical constructor                | Canonical constructor    | Tie         |
| **Abstract types**  | Classpath scanning for impls         | Returns null             | Easy Random |

### Customization Flexibility

| Aspect                      | Easy Random                                          | krandom                      | Winner      |
|-----------------------------|------------------------------------------------------|------------------------------|-------------|
| **Per-type customization**  | ✅ `randomize(Class, randomizer)`                     | ✅ `typeOverrides`            | Tie         |
| **Per-field customization** | ✅ Predicate-based + @Randomizer                      | ✅ `fieldOverrides` (by name) | Easy Random |
| **Exclusion mechanism**     | ✅ Predicate-based + @Exclude                         | No built-in                | Easy Random |
| **Context awareness**       | ✅ `ContextAwareRandomizer`                           | ✅ ContextualGenerator<T>     | Tie         |
| **Registry system**         | ✅ Layered with priority                              | No registry                | Easy Random |
| **Extension points**        | ✅ 4 interfaces (Provider, Registry, Policy, Factory) | ❌ Limited                    | Easy Random |

### Bean Validation Support

| Aspect                  | Easy Random                                                   | krandom                            | Winner  |
|-------------------------|---------------------------------------------------------------|------------------------------------|---------|
| **Constraint support**  | ✅ 10+ annotations (`@NotNull`, `@Size`, `@Min`, `@Max`, etc.) | ✅ 8 annotations (core constraints) | Tie     |
| **Separate module**     | ✅ `easy-random-bean-validation`                               | ✅ Inline in core jar               | krandom |
| **Constraint priority** | ✅ Overrides global config                                     | ✅ BV runs before built-in fallback | Tie     |

### Collection Support

| Aspect           | Easy Random                       | krandom                          | Winner      |
|------------------|-----------------------------------|----------------------------------|-------------|
| **Arrays**       | ✅ Populated with elements         | ✅ Populated via ObjectGenerator  | Tie         |
| **List types**   | ✅ ArrayList, LinkedList           | ✅ ArrayList with typed elements  | Tie         |
| **Set types**    | ✅ HashSet, TreeSet, LinkedHashSet | ✅ HashSet with typed elements    | Easy Random |
| **Map types**    | ✅ HashMap, TreeMap, etc.          | ✅ HashMap with typed keys/values | Easy Random |
| **Queue types**  | ✅ ArrayDeque, PriorityQueue       | ✅ Common queue types via object generation | Tie |
| **Size control** | ✅ `collectionSizeRange(min, max)` | ✅ `collectionSize(min, max)`     | Tie         |

### Realistic Data Generation

| Aspect                    | Easy Random                    | krandom                                          | Winner  |
|---------------------------|--------------------------------|--------------------------------------------------|---------|
| **DataFaker integration** | ✅ 20+ realistic randomizers    | ✅ 50+ built-in generators (no extra dep)         | krandom |
| **Personal data**         | ✅ Names, emails, phones        | ✅ Names, email, phone, birthday, national IDs    | Tie     |
| **Address data**          | ✅ Streets, cities, states, ZIP | ✅ City, state, country, ZIP, coordinates, street | Tie     |
| **Finance data**          | No                           | ✅ Credit cards, money, currency pairs, FX pairs  | krandom |
| **Network data**          | ✅ IPv4, IPv6, MAC              | ✅ IPv4, IPv6, MAC address                        | Tie     |
| **Company data**          | ✅ Company names                | ✅ CompanyNameGenerator (with/without suffix)     | Tie     |
| **Text generation**       | ✅ Paragraphs, sentences, words | ✅ LoremIpsumGenerator (WORD/SENTENCE/PARAGRAPH)  | Tie     |
| **Book identifiers**      | No                           | ✅ ISBN-10, ISBN-13 (check-digit valid)           | krandom |
| **Full names**            | ✅ Via DataFaker                | ✅ FullNameGenerator (composes First + Last)      | Tie     |
| **Locale support**        | ✅ Via DataFaker                | ✅ 10 locales across all realistic generators     | Tie     |

### Configuration & Defaults

| Aspect              | Easy Random         | krandom                 | Winner                      |
|---------------------|---------------------|-------------------------|-----------------------------|
| **Seed / PRNG**     | ✅ Default: 123L     | ✅ Unseeded fast `Random`; `random(Random)` and `secureRandom()` opt-ins | Easy Random (deterministic) |
| **Charset**         | ✅ Default: US_ASCII | ✅ Default: US_ASCII     | Tie                         |
| **String length**   | ✅ [1, 32]           | ✅ [5, 20]               | Preference                  |
| **Collection size** | ✅ [1, 100]          | ✅ [1, 10]               | Preference                  |
| **Max depth**       | ✅ Integer.MAX_VALUE | ✅ 5                     | krandom (safer)             |
| **Builder API**     | ✅ Fluent            | ✅ Fluent                | Tie                         |

---

## IMPLEMENTATION RECOMMENDATIONS

### Phase 1: CRITICAL OBJECT GENERATION GAPS ✅ DONE

**1. Circular Reference Handling** ✅ DONE

- ~~**Gap**: krandom can stack overflow on circular object graphs~~
- **Status**: ObjectPool implemented; circular references handled
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

**2. Array Type Support** ✅ DONE

- ~~**Gap**: krandom doesn't populate arrays (returns null)~~
- **Status**: Arrays auto-populated via ObjectGenerator
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

**3. Collection Type Support** ✅ DONE

- ~~**Gap**: krandom doesn't auto-populate List/Set/Map fields~~
- **Status**: List, Set, Map auto-populated via ObjectGenerator
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

**4. Objenesis Integration** ✅ DONE

- ~~**Gap**: krandom requires no-arg constructor; fails on Lombok @AllArgsConstructor, immutable classes~~
- **Status**: Objenesis fallback implemented in ObjectGenerator
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

**Total Phase 1: ~7 days** ✅ COMPLETE

---

### Phase 2: CUSTOMIZATION & EXCLUSION (Partial — Context done; exclusion/annotations remain)

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

**3. Context-Aware Generators** ✅ DONE

- ~~**Gap**: Generators can't access field path, depth, parent object~~
- **Status**: `ContextualGenerator<T>` + `GenerationContext` (fieldName, ownerType, depth)
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

**Total Phase 2: ~6 days** — Context done ✅; Field exclusion + @Randomizer annotation still pending

---

### Phase 3: BEAN VALIDATION INTEGRATION ✅ DONE

**1. JSR 380 Constraint Support** ✅ DONE

- ~~**Gap**: No validation annotation support~~
- **Status**: 8 constraints implemented inline in core — `@Size`, `@Min`, `@Max`, `@Positive`, `@PositiveOrZero`, `@Negative`, `@NegativeOrZero`, `@Email`, `@Pattern`, `@DecimalMin`, `@DecimalMax`
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

**Total Phase 3: ~5 days** ✅ COMPLETE

---

### Phase 4: DATE/TIME GENERATORS ✅ DONE

**1. JSR 310 (java.time) Randomizers** ✅ DONE

- ~~**Gap**: No date/time type support~~
- **Status**: `DateGenerator`, `TimeGenerator`, `LocalDateTimeGenerator`, `InstantGenerator`, `ZonedDateTimeGenerator` all implemented
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

**2. Date Range Configuration** ✅ DONE

- ~~**Gap**: No global date range configuration~~
- **Status**: `ObjectGeneratorConfig.dateRange(LocalDate, LocalDate)` implemented
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

**Total Phase 4: ~4 days** ✅ COMPLETE

---

### Phase 5: REALISTIC DATA GENERATORS ✅ DONE

**1. Email Generator** ~~DONE~~ ✅

- **Implemented**: `EmailGenerator` (7 formats: simple, domain-specific, first.last, UUID-based, safe, custom domain, locale-aware)

**2. UUID Generator** ~~DONE~~ ✅

- **Implemented**: `UUIDGenerator` (UUID v4 with seeded support)

**3. Regex-Based Generator** ~~⚡ HIGH PRIORITY~~ ✅ DONE

- ~~**Gap**: Can't generate from patterns~~
- **Status**: `RegexGenerator` implemented using `dk.brics.automaton` / generex

**4. Lorem Ipsum Generators** ~~⚠️ MEDIUM PRIORITY~~ ✅ DONE

- ~~**Gap**: No text content generation~~
- **Status**: `LoremIpsumGenerator` with `Mode.WORD`, `Mode.SENTENCE`, `Mode.PARAGRAPH`

**5. Name Generators** ~~DONE~~ ✅

- **Implemented**: `FirstNameGenerator` + `LastNameGenerator` (10 locales, gender-aware)

**6. Address Components** ~~DONE~~ ✅

- **Implemented**: `CityGenerator`, `StateGenerator`, `CountryGenerator`, `PostalCodeGenerator`, `CoordinateGenerator` (all 10 locales)

**7. Credit Card Generator** ~~DONE~~ ✅

- **Implemented**: `CreditCardGenerator` (Visa/MC/Amex/Discover, Luhn-valid, with CVV and expiry generators)

**Total Phase 5: ~~7 days~~ Completed ✅**

---

### Phase 6: ADVANCED FEATURES (Partial)

**1. BigDecimal / BigInteger Generators** ✅ DONE

- ~~**Effort**: 1 day~~ Implemented — `BigDecimalGenerator` + `BigIntegerGenerator`

**2. Optional\<T\> Support** ✅ DONE

- **Effort**: 0.5 days
- **Value**: Java 8+ null safety

**3. Classpath Scanning for Abstract Types** ❌ Pending

- **Effort**: 3 days (+ ClassGraph dependency)
- **Value**: Limited (slow, complex)

**4. ServiceLoader Registry Discovery** ❌ Pending

- **Effort**: 2 days
- **Value**: Plugin architecture

**Total Phase 6: ~6.5 days** — BigDecimal/BigInteger/Optional done ✅; scanning/SPI pending

---

## EFFORT ESTIMATES SUMMARY

| Phase     | Focus Area                                                             | Effort                   | Priority   | Status                                                                      |
|-----------|------------------------------------------------------------------------|--------------------------|------------|-----------------------------------------------------------------------------|
| Phase 1   | Object generation gaps (circular refs, arrays, collections, Objenesis) | 7 days                   | ⚡ CRITICAL | ✅ DONE                                                                      |
| Phase 2   | Customization & exclusion (predicates, @Randomizer, context)           | 6 days                   | HIGH       | ✅ DONE (partial)                                                            |
| Phase 3   | Bean validation integration                                            | 5 days                   | MEDIUM     | ✅ DONE                                                                      |
| Phase 4   | Date/time generators                                                   | 4 days                   | MEDIUM     | ✅ DONE                                                                      |
| Phase 5   | Realistic data (email, UUID, regex, lorem, names, addresses, cards)    | 7 days                   | MEDIUM     | ✅ DONE                                                                      |
| Phase 6   | Advanced features (BigDecimal, Optional, scanning, SPI)                | 6.5 days                 | LOW        | ✅ DONE (partial — BigDecimal/BigInteger/Optional done; scanning/SPI remain) |
| **TOTAL** |                                                                        | **28.5 days** (~6 weeks) |            | **~85% DONE**                                                               |

### Remaining Gaps

1. **Queue type support** (ArrayDeque, PriorityQueue) — LOW priority
2. **RandomizerProvider / RandomizerRegistry / ExclusionPolicy / ObjectFactory** extension points — LOW priority
3. **Classpath scanning** for abstract types/interfaces — LOW priority
4. **ServiceLoader** registry auto-discovery — LOW priority

---

## KEY DIFFERENTIATORS

### Easy Random Strengths (vs krandom)

1. **Object Pool for Circular References** 🏆
    - Prevents stack overflow on self-referential objects
    - Configurable pool size (default: 10)
    - krandom will crash on: `Person.spouse -> Person.spouse -> ...`

2. **Objenesis Integration** ~~🏆~~ ✅ (krandom parity achieved)
    - Both now use Objenesis fallback for classes without no-arg constructors
    - Works with Lombok `@AllArgsConstructor`, immutable classes

3. **Array & Collection Auto-Population** ~~🏆~~ ✅ (krandom parity achieved)
    - Both automatically populate `List<T>`, `Set<T>`, `Map<K,V>`, `T[]` fields
    - Queue types (ArrayDeque, PriorityQueue) still Easy Random only

4. **Bean Validation Support** ~~🏆~~ ✅ (krandom parity achieved)
    - Both generate data satisfying `@Size`, `@Min`, `@Max`, `@Email`, `@Pattern`, etc.
    - Easy Random uses separate module; krandom includes inline in core jar

5. **Exclusion Mechanisms** 🏆
    - Field predicates: `named("password")`, `ofType(Secret.class)`, `inClass(User.class)`
    - `@Exclude` annotation
    - Custom `ExclusionPolicy` interface
    - krandom has no built-in exclusion

6. **Predicate-Based Customization** 🏆
    - Target specific fields: `FieldPredicates.named("email").and(inClass(Person.class))`
    - Composable: `and()`, `or()`, `negate()`
    - krandom uses simple string matching

7. **Context-Aware Generators** ~~🏆~~ ✅ (krandom parity achieved)
    - Both support context-aware generation with field name, owner type, and depth
    - Easy Random additionally exposes root object, current object, and full field path
    - krandom `ContextualGenerator<T>` covers the most common use cases

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

11. **Comprehensive Date/Time Support** ~~🏆~~ ✅ (krandom parity achieved)
    - Both support `LocalDate`, `LocalTime`, `LocalDateTime`, `Instant`, `ZonedDateTime`
    - Both support global date range configuration via `ObjectGeneratorConfig`
    - Easy Random additionally has `OffsetDateTime`, `Year`, `YearMonth`, `Duration`, `Period`

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

4. **Explicit Determinism** 🎯
    - Fast unseeded random by default, seeded config for repeatability, `random(Random)` for caller-owned PRNGs, `secureRandom()` for explicit secure PRNG use
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
4. ❌ Classpath scanning for abstract/interface types - slow and edge-case heavy
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
