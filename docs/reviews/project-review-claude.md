# kRandom Project Review

> Reviewed: 2026-03-28 | Reviewer: Claude Sonnet 4.6

---

## Executive Summary

**kRandom** is a well-engineered, feature-rich random data generation library with exceptional documentation and test coverage. The Java core is sophisticated — reflection-based object generation, locale-aware providers, flexible configuration — with clean language wrappers for Kotlin and Scala. Code quality is enforced via strict coverage thresholds and Javadoc validation, and the API is both beginner-friendly (via `Generators` facade) and advanced-friendly (via registries, overrides, and composition).

**Overall verdict:** Mature, production-quality library. Rivals DataFaker and Bogus in coverage breadth. Main gaps are global mutable state, a large if-else chain in `FieldGeneratorResolver`, and incomplete Maven Central migration.

---

## Table of Contents

1. [Project Structure](#1-project-structure)
2. [Build System](#2-build-system)
3. [API Design](#3-api-design)
4. [Source Code Quality](#4-source-code-quality)
5. [Test Quality & Coverage](#5-test-quality--coverage)
6. [Documentation](#6-documentation)
7. [Locale & Internationalization](#7-locale--internationalization)
8. [Notable Implementations](#8-notable-implementations)
9. [Strengths](#9-strengths)
10. [Issues & Areas for Improvement](#10-issues--areas-for-improvement)
11. [Missing Features](#11-missing-features)
12. [Trajectory & Recommendations](#12-trajectory--recommendations)

---

## 1. Project Structure

```
krandom/
├── core/               ← All Java generators (233 source files, 149 test files)
├── java-api/           ← Thin Java facade (Phase 2, stub)
├── kotlin-api/         ← Kotlin extension functions (functional)
├── scala-api/          ← Scala 3 wrappers (functional)
├── examples/           ← Consumer examples: Java+Gradle, Java+Maven, Kotlin+Gradle,
│                           Kotlin+Maven, Scala+sbt, Scala+Mill
├── docs-site/          ← GitHub Pages documentation site
├── docs/               ← Architecture docs, feature-parity analysis, plans
└── scripts/            ← pre_commit_check.sh
```

**Pros:**
- Java-first design with language-specific wrappers is the correct architectural choice — core logic is isolated and independently testable
- Examples covering 6 build-tool/language combinations is a real differentiator for adoption
- Clear module boundaries: consumers of `kotlin-api` never need to depend on `core` directly

**Cons:**
- `java-api` is still a stub; Java consumers currently depend directly on `core`, which leaks internal API surface
- `scala-api` exists but Scala adoption requires `core` to be published to Maven Central first (blocked)

---

## 2. Build System

**Stack:** Gradle 8.12.1 · Kotlin 2.3.20 · Java 21 (Temurin) · JaCoCo 0.8.12

### Strengths

- **Version catalog** (`gradle/libs.versions.toml`) pins every dependency — zero floating versions
- **JaCoCo enforcement:** 99% line *and* branch coverage minimum, checked pre-commit and in CI
- **Spotless:** MIT license headers automatically stamped/verified; Kotlin + Java + Markdown formatting enforced
- **Javadoc:** `-Xdoclint:all,-missing -Werror` — undocumented public API is a build failure
- **Pre-commit script** (`scripts/pre_commit_check.sh`) runs formatting, tests, coverage, and Javadoc validation before any commit lands
- **Parallel builds** enabled; configure-on-demand enabled

### Issues

- `org.gradle.java.home` is hardcoded to a local Temurin path in `gradle.properties` — breaks any other machine without that exact JDK path; should use Gradle toolchain auto-provisioning instead
- Test task uses `-Xmx512m` to handle 50k+ dynamically registered tests — this is a symptom rather than a fix; the test suite architecture is worth revisiting (see §5)
- `org.cadixdev.licenser` 0.6.1 commented out due to StackOverflowError with Gradle 8.x — leaving a disabled plugin block as a comment adds noise; remove it entirely

---

## 3. API Design

### Entry Points

`Generators` is a static facade with 154+ factory methods:

```java
// Primitives
Generators.ofInt(1, 100)
Generators.ofBoolean()

// Locale-aware data
Generators.ofFirstName(Locale.FRANCE)
Generators.ofCity("en_US")

// Object graphs
Generators.ofObject(Person.class, config)

// Finance
Generators.ofCreditCard()
Generators.ofIban()
```

**Verdict:** Excellent discoverability. One import, one class, everything visible via IDE autocomplete.

### Core Abstractions

| Interface | Role |
|-----------|------|
| `Generator<T>` | `@FunctionalInterface` with `generate()`, `generateList()`, `stream()`, `map()`, `filter()` defaults |
| `BoundedGenerator<T>` | Adds `generate(T min, T max)` for ranged types |
| `ContextualGenerator<T>` | Field-aware generation with `GenerationContext` (type, field name, depth, owner) |

**Strengths:**
- `Generator` composition via `map()` / `filter()` is idiomatic and powerful
- `ContextualGenerator` enables genuinely smart object population (e.g., generate a realistic email based on the adjacent `firstName` field)
- Half-open `[min, max)` convention is explicit and consistent everywhere

**Issues:**
- `BoundedGenerator` and `Generator` are separate hierarchies; a generator is either bounded or not, which forces callers to know the type. A single `Generator<T>` with optional bounds could simplify this
- No `GeneratorFactory<T>` interface — makes dependency injection of generators awkward without referencing concrete classes

### Configuration

```java
GeneratorConfig config = GeneratorConfig.builder()
                                        .seed(42L)
                                        .locale(Locale.GERMANY)
                                        .stringLengthRange(5, 20)
                                        .collectionSizeRange(1, 10)
                                        .build();
```

**Strengths:** Immutable builder, sensible defaults, clearly documented constraints

**Issues:** No way to inherit/compose configs (e.g., "base config + override locale") — must re-specify all fields

### Extensibility

```java
// Register a custom locale data provider
FirstNameDataRegistry.register(myProvider);

// Use provider hub for domain-specific providers
ProviderHub hub = new ProviderHub(Locale.JAPAN);
hub.register("custom_domain", config -> new MyGenerator(config));

// Override specific fields during object generation
ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
                                                    .override(String.class, () -> "fixed")
                                                    .override(Person.class, "email", ctx -> ctx.ownerField("firstName") + "@example.com")
                                                    .excludeField("password")
                                                    .build();
```

**Verdict:** The three-level extensibility (registry → hub → per-object override) is well-designed and covers most real-world customisation needs.

---

## 4. Source Code Quality

### Design Patterns

| Pattern | Where Used | Assessment |
|---------|-----------|------------|
| Factory Method | `Generators.of*()`, `GeneratorConfig.builder()` | Correct, consistent |
| Builder | `GeneratorConfig`, `ObjectGeneratorConfig` | Immutable builders done right |
| Registry | All `*DataRegistry` classes | Thread-safe via `ConcurrentHashMap`, static init |
| Provider | All `*DataProvider` interfaces | Clean SPI-style extension |
| Adapter | Objenesis wrapping | Necessary evil, well contained |
| Predicate | `ObjectGeneratorConfig.excludeField(Predicate)` | Idiomatic Java |

### Naming Conventions

Consistent throughout:
- `{Domain}Generator` — the main generator class
- `{Domain}DataProvider` — the interface for locale data
- `BuiltIn{Domain}DataProvider` — the default implementation
- `{Domain}DataRegistry` — thread-safe static registry

### Immutability

- Final classes throughout core
- Public methods return `Collections.unmodifiableList()` / `List.copyOf()`
- Builders separate mutable-construction phase from immutable-use phase

### Error Handling

- `Objects.requireNonNull()` at every public boundary — no silent NPEs
- `IllegalArgumentException` with meaningful messages for constraint violations
- Domain exceptions: `ObjectGenerationException`, `SchemaGenerationException`

### Code Smell: Global Mutable State

Every `*DataRegistry` holds a static `ConcurrentHashMap`. This is thread-safe at runtime but:

- Makes unit-test isolation impossible (a test registering a mock provider affects all subsequent tests in the same JVM)
- Prevents multiple independent `kRandom` configurations in the same application
- Consider an instance-based `DataRegistryContext` that can be scoped per `GeneratorConfig`

### Code Smell: `FieldGeneratorResolver` if-else chain

`FieldGeneratorResolver.resolveGenerator()` contains a massive sequential if-else for 80+ types. This makes it:
- Hard to read and maintain
- Impossible to extend without modifying the class
- A single-point-of-failure for new type support

**Recommendation:** Replace with a `Map<Class<?>, Supplier<Generator<?>>>` that can be populated at static init time (and at runtime for extensibility).

### Code Smell: `USE_NATURAL_NUMBER_CACHE`

A public mutable top-level `var` in `NaturalNumberGenerator.kt`. This is a shared global flag with no thread-safety guarantees. Should either be removed (if the cache is always beneficial) or replaced with a proper configuration option.

---

## 5. Test Quality & Coverage

### Stats

Reviewed on 2026-03-28.

## Scope

- Full repository walk-through with focus on `core` architecture, locale/data registries, object generation, and build pipeline.
- Validation run: `./scripts/pre_commit_check.sh` passed fully (formatting, compile, tests, javadoc, coverage).
- Current quality gate result: 100.0% line coverage and 100.0% branch coverage.

## Findings (Prioritized)

### 1. High: `ObjectGenerator` can fail for custom concrete collection subtypes

- In `FieldGeneratorResolver`, unknown concrete `List` subtypes are materialized as `ArrayList` fallback (`toListType`):
  `core/src/main/java/io/github/frikit/krandom/generator/object/FieldGeneratorResolver.java:287-289`
- Unknown `Queue`/`Map` concrete subtypes are also fallback-converted (`toQueueType`, `toMapType`):
  `core/src/main/java/io/github/frikit/krandom/generator/object/FieldGeneratorResolver.java:294-312`
- Assignment happens reflectively and requires assignable runtime type:
  `core/src/main/java/io/github/frikit/krandom/generator/object/ObjectGenerator.java:201-207`
- Impact: a field declared as a custom concrete subtype (not one of the handled built-ins) can throw `ObjectGenerationException` even though the field type is valid.
- Improvement: instantiate declared concrete collection types when possible (no-arg constructor path), then populate; use fallback only for interfaces/abstract types.

### 2. Medium: Registry input validation is inconsistent across user data registries

- `FirstNameDataRegistry`, `LastNameDataRegistry`, `GenderDataRegistry`, `TitleDataRegistry`, `SuffixDataRegistry` validate only provider/locale non-null in `register(...)`:
  `core/src/main/java/io/github/frikit/krandom/generator/user/FirstNameDataRegistry.java:54-65`
  `core/src/main/java/io/github/frikit/krandom/generator/user/LastNameDataRegistry.java:54-65`
  `core/src/main/java/io/github/frikit/krandom/generator/user/GenderDataRegistry.java:54-65`
  `core/src/main/java/io/github/frikit/krandom/generator/user/TitleDataRegistry.java:65-78`
  `core/src/main/java/io/github/frikit/krandom/generator/user/SuffixDataRegistry.java:53-64`
- `ProfessionDataRegistry` and `StreetAddressDataRegistry` perform strict content validation (non-empty, non-blank, valid weights):
  `core/src/main/java/io/github/frikit/krandom/generator/user/ProfessionDataRegistry.java:132-155`
  `core/src/main/java/io/github/frikit/krandom/generator/location/StreetAddressDataRegistry.java:44-47,81-92`
- Downstream behavior is inconsistent when bad provider data is registered:
  - `FirstNameGenerator`/`LastNameGenerator` can fail on `nextInt(0)` with empty arrays:
    `core/src/main/java/io/github/frikit/krandom/generator/user/FirstNameGenerator.java:88-99`
    `core/src/main/java/io/github/frikit/krandom/generator/user/LastNameGenerator.java:73-75`
  - `TitleGenerator`/`SuffixGenerator` silently return empty string when array is empty:
    `core/src/main/java/io/github/frikit/krandom/generator/user/TitleGenerator.java:56-59`
    `core/src/main/java/io/github/frikit/krandom/generator/user/SuffixGenerator.java:74-77`
- Improvement: enforce one registry contract (validate at registration), and keep runtime generator behavior consistent.

### 3. Medium: Locale fallback policy is inconsistent across location generators

- `CountryDataRegistry` supports exact + language fallback (`en_CA` can resolve via `en`):
  `core/src/main/java/io/github/frikit/krandom/generator/location/CountryDataRegistry.java:84-106`
- `CityDataRegistry` and `StateDataRegistry` are exact-key only:
  `core/src/main/java/io/github/frikit/krandom/generator/location/CityDataRegistry.java:60-65,89-91`
  `core/src/main/java/io/github/frikit/krandom/generator/location/StateDataRegistry.java:60-65,89-91`
- `StreetAddressDataRegistry` is also exact-key only:
  `core/src/main/java/io/github/frikit/krandom/generator/location/StreetAddressDataRegistry.java:53-58,77-79`
- Impact: same locale can be accepted by one location generator and rejected by another, which is surprising at API level.
- Improvement: choose one policy (exact-only or exact+language fallback) for all location registries and enforce with shared tests.

### 4. Medium: Global mutable registries create cross-test and cross-context coupling

- Registries use static mutable maps and global `register(...)` mutation:
  - `FirstNameDataRegistry`: `REGISTRY` static map
    `core/src/main/java/io/github/frikit/krandom/generator/user/FirstNameDataRegistry.java:33-40`
  - `CityDataRegistry`: static `providers` map
    `core/src/main/java/io/github/frikit/krandom/generator/location/CityDataRegistry.java:29-35`
- No reset/snapshot API exists for isolating temporary overrides.
- Impact: custom provider registration affects entire JVM process; this makes test isolation and multi-tenant embedding harder.
- Improvement: add an optional scoped registry context (`GeneratorConfig`/`ProviderHub` bound), while preserving current global default for backward compatibility.

### 5. Low: Build reproducibility risk from unconditional `mavenLocal()`

- Global repository order prefers local artifacts before central:
  `build.gradle.kts:14-17`
- Impact: local cached artifacts can shadow central dependencies and hide integration issues.
- Improvement: gate `mavenLocal()` behind explicit opt-in property (for local dev only).

## Strengths

- Excellent quality gates: pre-commit script enforces formatting, compile, tests, javadocs, and coverage (`scripts/pre_commit_check.sh`).
- Coverage discipline is very strong (currently 100%/100%).
- Locale architecture improved with a clear shared source of truth in `SupportedLocale`:
  `core/src/main/java/io/github/frikit/krandom/generator/locale/SupportedLocale.java`
- Locale completeness is actively enforced by coverage tests:
  `core/src/test/java/io/github/frikit/krandom/generator/locale/SupportedLocaleCoverageTest.java`
- Modular project structure is clean (`core`, Java/Kotlin/Scala API modules, examples, docs).

## Recommended Improvement Plan

1. Stage 1: Fix collection subtype assignment in `ObjectGenerator` and add regression tests for custom concrete list/queue/map subclasses.
2. Stage 2: Standardize registry validation rules; reject invalid provider payloads at registration time across all user registries.
3. Stage 3: Unify location fallback behavior (exact-only or exact+language) and codify it with shared parameterized tests.
4. Stage 4: Introduce optional scoped registries/context to reduce global mutable-state coupling.
5. Stage 5: Make `mavenLocal()` opt-in to improve build reproducibility.
