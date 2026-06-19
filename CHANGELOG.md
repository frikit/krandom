# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

## [1.2.0] - 2026-06-19

### Added
- `GeneratorConfig.objectSubtype(declaredType, implementationType)` and `ObjectGeneratorConfig.subtype(...)`: explicit mapping of abstract/interface field types to concrete implementations during object generation (kRandom's answer to classpath scanning in other libraries).
- `Generators.ofDataFakerExpression(...)`: migration adapter resolving DataFaker-style `#{Provider.method}` expressions (case-insensitive, camelCase or snake_case) through the `ProviderHub`.
- **JUnit 5 extension module** (`krandom-junit`): `KrandomExtension` fixes the seed per test, injects seeded `GeneratorConfig`/`GeneratorConfig.Builder` parameters, and reports the seed on failure (JUnit report entry + `System.err` reproduction hint); `@KrandomSeed(value|text)` pins seeds at method or class level.

### Changed
- Core logging now uses the SLF4J facade instead of `java.util.logging`, so kRandom diagnostics flow through the consuming application's configured logging backend without a JUL-to-SLF4J bridge. The `ignoreErrors` swallowed-failure diagnostics are now logged at SLF4J `DEBUG` (previously `java.util.logging` FINE).

### Fixed
- `krandom-core` no longer ships a logging backend or configuration to consumers: removed the packaged `logback.xml` (which set the root logger to DEBUG and wrote `logs/krandom.log`) and dropped `logback-classic` from the runtime classpath, where it could hijack or conflict with the consuming application's logging.
- `LuhnGenerator` payload digits now span the full `[0, 9]` range; previously they were drawn from `[1, 9]`, so `0` never appeared in the first nine positions.
- `BigDecimalGenerator` now fails fast with `IllegalArgumentException` when a bound overflows `long` after scaling, instead of silently narrowing via `longValue()` and emitting out-of-range or invalid values.
- `RegexGenerator` rejects explicit `{n}` / `{n,m}` repetition counts above 10 000 at construction time, preventing unbounded output expansion (e.g. `a{2000000000}`) from exhausting memory.
- Object-graph generation sorts settable fields by name before allocating per-field seeds, so seeded output is reproducible across JDK builds, vendors, and instrumentation agents rather than depending on unspecified `Class.getDeclaredFields()` ordering.
- Corrected the inaccurate "thread-safe and can be shared across threads" Javadoc on 17 scalar generators (UUID, email, date/time, network, currency, …) to match the `Generator` contract: instances hold a single mutable PRNG and are not safe to share across threads. Added a "Concurrency and determinism" section to the README documenting the one-instance-per-thread pattern and the determinism/sharing trade-off.

## [1.1.0] - 2026-05-10

### Added
- Configurable random source on `GeneratorConfig`: `builder().random(Random)` for client-owned PRNG instances and `builder().secureRandom()` for `SecureRandom`-backed generation.
- Deterministic temporal generation: `GeneratorConfig` now carries a `Clock`, making date/time, card-expiration, and birthday generators reproducible under a fixed clock and seed.
- `Schema.writeTo(OutputStream | Writer, OutputFormat, count[, tableName])` streaming export overloads.

### Changed
- `ProviderHub` is now thread-safe: registries are backed by concurrent maps and single registrations are atomic under `ConflictPolicy.FAIL`; the thread-safety contract is documented in the class Javadoc.
- `objectIgnoreErrors=true` / `ignoreErrors=true` no longer swallows population failures fully silently: each ignored failure is logged (at `java.util.logging` FINE in 1.1.0; migrated to SLF4J `DEBUG` in 1.2.0) for diagnosability.
- Hardened object generation internals: `FieldGeneratorResolver`, `SemanticCoherenceAdjuster`, and `SemanticFieldRegistry` refactored for stricter semantic-alias resolution and edge-case coverage.
- Consumer API polish across `Generator` and `Schema`; module metadata refreshed for published artifacts.
- Kotlin updated to 2.4.0; Gradle wrapper downloads made resilient (longer timeout, retries).

### Removed
- jqwik integration module (`jqwik-extensions`); property-based testing support continues via `kotest-extensions`.

## [1.0.0] - 2026-05-08

First public release on Maven Central under `io.github.frikit`.

### Added
- **Spring Boot starter module** (`spring-boot-starter`) with auto-configuration, externalized `krandom.*` properties, and `KrandomObjectFakerFactory` bean.
- **Comparative JMH benchmarks** against DataFaker, JavaFaker, EasyRandom, and Instancio for scalar, object, and bulk generation.
- Monthly benchmark report convention under `docs/benchmarks/`.
- Schema export formats: CSV, JSONL, XML, SQL via `Schema.toCsv()`, `toJsonl()`, `toXml()`, `toSqlInserts()`.
- Schema export is now side-effect free (no internal state mutation).
- Locale facade overloads for schema providers.
- Nationality and identity edge-case handling in schema exports.
- `SECURITY.md` (vulnerability reporting via GitHub Security Advisories), `.github/ISSUE_TEMPLATE/` (bug + feature), and `.github/PULL_REQUEST_TEMPLATE.md`.
- Release runbook at `docs/release-runbook.md`.

### Changed
- Spring Boot starter uses `java-library` plugin with `api` scope for transitive dependency exposure.
- Spring Boot starter is now built against Spring Boot 4.x (`spring-boot-autoconfigure 4.0.6`); consumer applications must be on Spring Boot 4.x.
- Benchmark README restructured to lead with competitor comparison results.
- README updated with performance section, Spring Boot starter in modules table, and published artifact list.
- POM `<description>` is now distinct per published module (Central Portal validation friendliness).
- Maven Central publishing migrated from legacy OSSRH (`s01.oss.sonatype.org`) to the Central Portal (`central.sonatype.com`) via the `com.gradleup.nmcp.settings` plugin. The release workflow now invokes `./gradlew publishAggregationToCentralPortal`.

### Deferred to a future patch
- `FieldGeneratorResolver` falls back to `ArrayList`/`HashMap` for unknown concrete `List`/`Queue`/`Map` subtypes; the declared concrete type's no-arg constructor should be tried first.
- Registry input validation is inconsistent across user data registries (`FirstName`/`LastName`/`Gender`/`Title`/`Suffix` accept content that downstream generators reject; `Profession`/`StreetAddress` validate at registration). Standardize on register-time validation.
- Locale fallback policy differs by registry: `CountryDataRegistry` does language fallback; `CityDataRegistry`/`StateDataRegistry`/`StreetAddressDataRegistry` are exact-match only. Pick one policy and codify it.

See [`docs/reviews/project-review-codex.md`](docs/reviews/project-review-codex.md) for full context.

---

## [0.x] - 2026-04-23

### Added
- **Structured commerce generators**: `OrderGenerator`, `InvoiceGenerator`, `ShipmentGenerator`, `PaymentGenerator`.
- **Company payload generation** with locale-quality enforcement.
- **Custom locale bundles** with isolated and global registration support.
- Examplify support in `TextFormatProvider`.
- API-selection guidance in public docs (`docs-site/guides/choosing-an-api.md`).
- Structural-vs-semantic benchmark baselines.
- Expanded locale support: promoted `nl_NL`, `pl_PL`, `tr_TR`, `hi_IN`, `ar_SA`, `sv_SE`, `nb_NO` to native built-in datasets.
- All remaining built-in fallback locales promoted to native datasets.

### Changed
- Public object-config usage converged; removed last internal split.
- Expanded locale support unified with hardened location validation.
- DataRegistryContext profession validation strengthened.

### Removed
- Deprecated APIs and deprecated Java locale usage removed.

---

## [0.x] - 2026-04-21

### Added
- **ObjectFaker API**: fluent fixture authoring with `ruleFor()`, nested path rules, include/ignore rules, named profiles.
- **Composite generators**: `AddressPayload`, `ContactPayload`, `PersonPayload` first-class generators.
- **Structured generators**: job, bank, card, and product payload generators.
- **Semantic object generation**: coherent address, money, age/status generation with ProviderHub integration.
- **ObjectGenerator enhancements**: semantic mode, nullability control, uniqueness constraints, field override support for same-name classes across packages.
- `GeneratorConfig` promoted as single source of truth for object-generation defaults.
- Advanced object overrides unified under `GeneratorConfig`.

---

## [0.x] - 2026-04-20

### Added
- **Schema DSL**: `Field` and `Schema` classes for row-style record generation.
- JSONL and CSV output support for Schema.
- Extensible schema token registration.
- Registry-backed schema templates and payload shells.
- ProviderHub taxonomy expansion with text formatting helpers.
- Locale quality tiers with coverage checks.
- Focused benchmarks for semantic objects and schema output.
- Migration guidance and release readiness documentation.

### Changed
- Near-100% coverage gates enforced (99.9% line, branch, instruction, method, class, complexity).

---

## [0.x] - 2026-04-18

### Added
- **Benchmarks module** with JMH and macro profiling.
- Social profile and handle generators with locale support.
- `ProfiledProviderFactory` with `Generators` profile support.
- `reseed()` method with reflective access support on `Generator`.
- Data provider interfaces updated to use `SupportedLocale` for built-in data seeding.

### Changed
- Object generation defaults unified and promoted into `GeneratorConfig`.
- Random number generation refactored to use `config.createRandom()` across all generators.
- Provider registration and validation streamlined across data registries.
- Kotlin and Scala API modules removed to keep implementation surface focused.

---

## [0.x] - 2026-03-13

### Added
- GitHub Actions release workflow for publishing to GitHub Packages.
- Consumer examples for Java (Gradle, Maven), Kotlin (Gradle, Maven), Scala (sbt, Mill).
- Kotlin API wrappers for generator configuration and providers.
- Scala API wrappers for generator configuration and providers.
- GitHub Pages documentation site with deployment workflow.

### Changed
- Group and artifact IDs updated to `io.github.frikit` namespace.

---

## [0.x] - 2026-03-03

### Added
- **National ID generators**: 10-country support (US SSN, GB NI, AU TFN, FR NIR, DE Steuer-ID, JP My Number, ES DNI, IT Codice Fiscale, BR CPF, CN Resident ID).
- **ProviderHub**: Mimesis-style generic provider hub with alias-based lookup and conflict policy.
- **Schema-based record generation**: `Field` and `FieldLookup` classes.
- Country generator enhancements: numeric codes, calling codes, continent, timezone.

---

## [0.x] - 2026-03-01

### Added
- City, state, and country convenience generators with locale-aware features.
- Text and finance generators (banking, text generation).
- SWIFT/BIC code generation with locale-aware vocabulary.
- `AvatarUrlGenerator` with domain name generation and custom phone format support.
- `BankAccountGenerator`, `BankNameGenerator`, `BankTypeGenerator` with locale-aware generation.

### Changed
- Locale handling simplified in generators; country code checks refactored.

---

## [0.x] - 2026-02-28

### Added
- **Selection combinators**: `PickGenerator`, `PickSetGenerator`, `ShuffleGenerator`, `UniqueGenerator`, `RepeatGenerator`, `WeightedGenerator`.
- **Name generators**: `FullNameGenerator`, `MiddleNameGenerator` with middle name support.
- **Profession generator** with locale-specific data.
- **Street address data** for multiple locales with secondary unit support.
- **File generators**: `FileExtensionGenerator`, `FileNameGenerator`.
- **Text generators**: `NextWordGenerator`, `WordGenerator`, `SyllableGenerator`, `SentenceGenerator`, `ParagraphGenerator` with locale-aware generation.
- `LoremIpsumGenerator` refactored with coherent word sequences.
- `IPv4Generator` and `IPv6Generator` with compatibility wrappers.
- `Randomizer` and `RandomizerArgument` annotations for customizable field generation.
- `ObjectGenerator` support for `Optional`, atomic types, JSR-310 date/time types.
- Username and password generators with range-based generation.
- IP and JobType generators with locale support.
- BIC and ISIN generators (SWIFT/BIC and ISIN codes).

### Changed
- Package structure refactored: Coin/Dice generators moved to `io.github.frikit.krandom.games.*`.

---

## [0.x] - 2026-02-25

### Added
- **Feature parity push**: implemented generators for numbers, booleans, chars, strings, suffixes, first/last names, age, birthday, country, city, state, postal code, phone, coordinates, credit cards, currency, card expiration, domain, email, IPv4/IPv6, URL, color, date/time, UUID, hash, dice.
- Selection generators from Chance.js parity.
- Easy Random parity: high, medium, and low priority features.
- `BigDecimal` and `BigInteger` generators.
- `FullName`, `StreetAddress`, `CompanyName`, `ISBN` generators.
- `MacAddressGenerator`.

---

## [0.x] - 2026-02-22

### Added
- **Modernization**: multi-module Gradle structure with `core/` module.
- Java 21 toolchain configuration.
- Spek2 to Kotest migration.
- Spotless formatting with MIT license headers.
- JaCoCo coverage enforcement.
- `pre_commit_check.sh` script for local quality checks.
- Logback logging (replaced log4j 1.x).

### Changed
- License changed from Apache 2.0 to MIT.
- Package moved to `io.github.frikit.krandom`.

---

## [0.x] - Pre-2026

### Added
- Initial random primitive generators: int, long, float, double, boolean, char, string.
- Fibonacci sequence generator.
- Luhn algorithm implementation.
- First name and surname generators with resource file loading.
- Username generator.
- Dice randomizer.
- Coin flip generator.
- Basic object population via reflection.
- JaCoCo code coverage.
- Travis CI and GitHub Actions CI.
- Gradle wrapper management.

[Unreleased]: https://github.com/frikit/krandom/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/frikit/krandom/releases/tag/v1.0.0
