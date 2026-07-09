# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added
- Structured, value-sanitized generation failure context shared by object and schema exceptions, with stable category, operation, path, type, depth, and record-index fields.
- `krandom-bom`, a Maven/Gradle platform that keeps all published kRandom modules on one version. Consumer examples now import the BOM and omit individual kRandom module versions.
- Japicmp compatibility and evolution gates check every published jar module against the configured latest-GA baseline (`1.5.0`) in local pre-commit checks, CI, and releases. Binary/source breaks fail independently, while additions or other public changes require exact reviewed classification.
- A generated HTML/XML public API inventory for every published jar module, plus a checked-in v2 disposition document covering facade aliases, registries, object generation, and integrations.
- Machine-readable release/module/locale/constraint/schema facts with a documentation gate that rejects stale versions, support counts, resource paths, and default-random claims.
- Immutable GitHub Action revisions plus checksum verification for the Gradle wrapper distribution and downloaded Mill launcher, enforced locally and in release CI.
- Strict Gradle dependency verification with reviewed SHA-256 metadata, centralized repositories, and rejection of dynamic or changing dependency selectors.
- Validated CycloneDX 1.6 JSON/XML SBOMs for every published module, attached automatically to GitHub releases.
- Pinned GitHub/Sigstore build-provenance attestations for the signed Maven Central bundle, jars, and SBOMs before publication.

### Changed
- `krandom-core` is now a Java-only build and no longer publishes an unused `kotlin-stdlib` runtime dependency. Kotlin remains confined to the Kotlin DSL and Kotest integration modules.
- Redundant `Generators` aliases for constants, selection, shuffle, and uniqueness are deprecated for removal in v2. Canonical replacements are documented in `docs/migration/v1.6-to-v2.md`; the legacy methods remain thin behavior-compatible delegates in 1.6.
- Maven Central publication now uses NMCP's explicit aggregation plugin and an exact seven-module graph, removing the convenience settings plugin's Gradle 10 deprecation.

### Fixed
- `Generator.map(...)` and `Generator.filter(...)` now preserve deterministic reseeding when their source implements `Seedable`; non-seedable sources remain honest and do not claim that capability.
- Object generation no longer swallows custom-map insertion failures and returns a partial map: strict mode reports sanitized indexed context, while explicit lenient mode discards the whole map.
- Concrete list, set, and queue insertion failures no longer return partial or unexplained values: strict mode reports sanitized field context, while explicit lenient mode discards the whole collection.
- Concrete collection and map constructors that throw now produce sanitized construction context in strict mode and `null` in explicit lenient mode; types without a no-arg constructor retain the compatibility fallback.
- Primitive-array element assignment now fails with sanitized indexed context in strict mode; explicit lenient mode retains the documented JVM default element.
- Direct fields with unsupported interface, abstract, `Object`, or JDK types now fail with sanitized structured context by default; explicit lenient mode retains the type-default fallback.

## [1.5.0] - 2026-06-22

### Added
- **21 new DataFaker-parity generators**, each exposed on the `Generators` facade with 100% line/branch coverage. The locale-aware vocabulary generators ship per-locale resource files for all 35 built-in locales (`krandom/<concept>/<locale>.txt`) with English fallback.
  - **Person attributes:** `BloodTypeGenerator` (`ofBloodType`, locale-weighted ABO/Rh), `ZodiacGenerator` (`ofZodiac`, Western signs + `signFor(date)`; localized across 35 locales), `ChineseZodiacGenerator` (`ofChineseZodiac`, animals + `animalFor(year)`; 35 locales), `PronounGenerator` (`ofPronoun`, subject/object sets; 35 locales), `MbtiGenerator` (`ofMbti`, 16 types + `withNickname()`), `NationalityGenerator` (`ofNationality`, demonyms; 35 locales), `HobbyGenerator` (`ofHobby`; 35 locales).
  - **Localized vocabulary (35 locales):** `MeasurementGenerator` (`ofMeasurement`, units), `FinancialTermGenerator` (`ofFinancialTerm`), `RestaurantTypeGenerator` (`ofRestaurantType`, cuisine/type), `WeatherGenerator` (`ofWeather`, conditions).
  - **Identifiers & formats:** `VinGenerator` (`ofVin`, ISO-3779 check digit) + `VehicleGenerator` (`ofVehicle`, make/model/plate); `CnpjGenerator` (`ofCnpj`) and `ofCpf()` for Brazilian company/person tax ids (check-digit valid); `PassportGenerator` (`ofPassport`); `DrivingLicenseGenerator` (`ofDrivingLicense`).
  - **Technical / universal:** `NatoPhoneticGenerator` (`ofNatoPhonetic`, ICAO; `wordFor`/`spell`), `ProgrammingLanguageGenerator` (`ofProgrammingLanguage`), `AwsGenerator` (`ofAws`; region/instanceId/s3Bucket), `AzureGenerator` (`ofAzure`; region/resourceGroup), `ComputerGenerator` (`ofComputer`; OS/platform/deviceType).
- `LocaleTextResourceLoader` is now `public` so locale-aware generators in any package can reuse the shared classpath resource loader.
- Full DataFaker provider catalog mapping all 256 providers to krandom status (`docs/feature-parity/datafaker-providers-catalog.md`), plus a competitive gap tracker, Instancio parity matrix, and migration guides (from JavaFaker/DataFaker/EasyRandom/Instancio).

### Changed
- Dependency bumps (verified no regression across tests, 100% coverage gate, consumer examples, and JMH benchmarks): `net.datafaker` 2.5.4→2.6.0, `com.diffplug.spotless` 8.6.0→8.7.0, `io.kotest` 6.1.11→6.2.1, `com.gradleup.nmcp.settings` 1.5.0→1.6.0; CI actions `actions/checkout` 6→7 and `softprops/action-gh-release` 3.0.0→3.0.1.
- Benchmark dashboard refreshed to a full-run JMH baseline; `run_benchmarks.sh` now redacts machine-local paths from captured result files.
- CHANGELOG pre-1.0.0 history condensed into a single summarized section.

## [1.4.0] - 2026-06-20

### Changed
- Gender labels, name suffixes, professions, and titles are now curated per-locale classpath resource files (`krandom/genders/<locale>.txt`, `krandom/suffixes/<locale>.txt`, `krandom/professions/<locale>.txt`, `krandom/titles/<locale>.txt`), loaded the same way as names — the last hardcoded language-string switches in the built-in user-data providers are gone, so adding or expanding a locale is a data edit, not a code change. **Relevant to consumers who load these classpath resources directly.** Gender and suffix data is migrated verbatim (generator behaviour unchanged); profession and title coverage is expanded (35 locales, 40 professions each, plus broader honorific/title sets). Profession ranked weights are now derived from list position, so the files can be any length.
- Build: Gradle wrapper upgraded 9.5.1 → 9.6.0.

### Fixed
- `CoordinatesGenerator` now renders coordinates as plain decimals via `BigDecimal.toPlainString()`: a near-zero value such as longitude `0.00044` previously serialized as `4.4E-4` and failed coordinate-format validation. Output never uses scientific notation and always uses `.` as the decimal separator regardless of locale.

## [1.3.0] - 2026-06-20

### Added
- Localized country datasets for all 35 supported locales (195 countries each). Previously only 10 locales had curated country files; the other 25 fell back to JDK-localized names.

### Changed
- Expanded every built-in locale dataset to richer, fully-unique coverage: cities ≥100 per locale, first (male/female) and last names ≥100 per locale, street names >25 per locale, plus comprehensive secondary-unit designators and paired long/short street types per locale.
- **Resource layout:** `names/` and `streets/` datasets are reorganized into per-type subfolders — `names/{first_male,first_female,last}/<locale>.txt` and `streets/{street_names,street_types_long,street_types_short,secondary_units}/<locale>.txt` (previously flat `<locale>_<type>.txt`). Relevant to consumers who load these classpath resources directly.
- Tightened locale coverage tests to lock in the new dataset floors (cities ≥100, names ≥100, countries ≥195, street names ≥26, street types ≥15) and to assert the long/short street-type lists stay paired (equal length) for every locale.

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

## [Pre-1.0.0] - 2026

Condensed summary of all pre-1.0.0 development (≈Feb–Apr 2026), originally tracked as ten dated `0.x` entries. See the git history for the full per-iteration breakdown.

### Added
- **Primitives & math**: int, long, float, double, boolean, char, string, `BigDecimal`/`BigInteger`, number ranges; Fibonacci, Luhn, hashing, UUID, dice, coin flip.
- **Identity & locale data**: first/middle/last/full names, age, birthday, gender, suffixes, professions, titles; country/city/state/postal code/phone/coordinates with locale-aware datasets and quality tiers; National ID generators for 10 countries (US SSN, GB NI, AU TFN, FR NIR, DE Steuer-ID, JP My Number, ES DNI, IT Codice Fiscale, BR CPF, CN Resident ID).
- **Internet & finance**: email, domain, URL, IPv4/IPv6, MAC address, username/password, social profile/handle, `AvatarUrlGenerator`; credit cards, currency, card expiration, IBAN/SWIFT-BIC, ISIN, bank account/name/type.
- **Text & codes**: word/syllable/sentence/paragraph/Lorem Ipsum, company name, street address, ISBN.
- **Selection combinators**: `PickGenerator`, `PickSetGenerator`, `ShuffleGenerator`, `UniqueGenerator`, `RepeatGenerator`, `WeightedGenerator` (Chance.js parity).
- **Object generation**: reflection-based population; `ObjectGenerator` (Optional, atomic, JSR-310, semantic mode, nullability, uniqueness, same-name overrides); `ObjectFaker` fluent API (`ruleFor()`, nested paths, include/ignore, named profiles); composite payloads (Address/Contact/Person) and structured payloads (job/bank/card/product/order/invoice/shipment/payment); `GeneratorConfig` as the single source of truth.
- **Schema & providers**: Schema DSL (`Field`/`Schema`/`FieldLookup`) with JSONL/CSV output and extensible tokens; `ProviderHub` (Mimesis-style alias lookup with conflict policy); custom locale bundles; `Randomizer`/`RandomizerArgument` annotations; `reseed()`.
- **Tooling & distribution**: multi-module Gradle (Java 21 toolchain), Spotless + MIT license headers, JaCoCo near-100% coverage gates (99.9% line/branch/instruction/method/class/complexity), `pre_commit_check.sh`, JMH benchmarks module; consumer examples (Java/Kotlin/Scala), GitHub Pages docs site, and a GitHub Actions release workflow.

### Changed
- Relicensed Apache 2.0 → MIT; moved to the `io.github.frikit.krandom` package and `io.github.frikit` group/artifact IDs.
- Unified random sourcing on `config.createRandom()` and object-generation defaults under `GeneratorConfig`; streamlined provider registration/validation; data providers seed from `SupportedLocale`.
- Promoted all built-in fallback locales to native datasets; simplified locale handling; migrated test stack Spek2 → Kotest; replaced log4j 1.x with Logback.

### Removed
- Kotlin and Scala API modules (implementation surface kept focused); deprecated APIs and deprecated Java locale usage.

[Unreleased]: https://github.com/frikit/krandom/compare/v1.5.0...HEAD
[1.5.0]: https://github.com/frikit/krandom/compare/v1.4.0...v1.5.0
[1.4.0]: https://github.com/frikit/krandom/compare/v1.3.0...v1.4.0
[1.3.0]: https://github.com/frikit/krandom/compare/v1.2.0...v1.3.0
[1.2.0]: https://github.com/frikit/krandom/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/frikit/krandom/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/frikit/krandom/releases/tag/v1.0.0
