# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added
- **Spring Boot starter module** (`spring-boot-starter`) with auto-configuration, externalized `krandom.*` properties, and `KrandomObjectFakerFactory` bean.
- **Comparative JMH benchmarks** against DataFaker, JavaFaker, EasyRandom, and Instancio for scalar, object, and bulk generation.
- Monthly benchmark report convention under `docs/benchmarks/`.
- Schema export formats: CSV, JSONL, XML, SQL via `Schema.toCsv()`, `toJsonl()`, `toXml()`, `toSqlInserts()`.
- Schema export is now side-effect free (no internal state mutation).
- Locale facade overloads for schema providers.
- Nationality and identity edge-case handling in schema exports.

### Changed
- Spring Boot starter uses `java-library` plugin with `api` scope for transitive dependency exposure.
- Benchmark README restructured to lead with competitor comparison results.
- README updated with performance section, Spring Boot starter in modules table, and published artifact list.

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

[Unreleased]: https://github.com/frikit/krandom/compare/HEAD
