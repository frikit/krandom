# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

## [2.4.0] - 2026-09-05

### Changed

- Isolate object-field stream planning behind a focused internal policy while preserving the
  public API, legacy seeded output, and explicit independent-stream behavior.
- Expand native-image qualification to cover locale-backed resources, record construction, and a
  configuration-scoped extension provider.
- Add the stream planner to critical-path mutation testing.

### Security

- Enforce reviewed SHA-256 verification metadata for resolved Gradle dependencies. Existing exact
  version, wrapper checksum, and immutable GitHub Action requirements remain in place.

### Documentation

- Clarify the supported Java `ObjectFaker` and Kotlin DSL capability boundaries and record the
  compatible 2.x fixture contract.

## [2.3.0] - 2026-09-04

### Added

- Explicit `GeneratorConfig.snapshotClock()` for sharing a captured instant between generation
  and replay diagnostics; existing configurations retain their live clocks.
- Opt-in `ObjectFieldStreamPolicy.INDEPENDENT` for seed-owned object generation, preserving
  unrelated structural field streams when rules, exclusions, or modules are installed. The
  default `LEGACY` policy and existing v1 recipe interpretation remain unchanged. Recipes record
  explicit policies; custom callbacks still do not claim portable replay.
- Kotlin DSL stream-policy configuration and JUnit's `krandom.junit.snapshot-clock=true` option.

### Fixed

- Failed profile application restores library-owned faker configuration, including nested profiles,
  rules, validation settings, and cached generator references. Callback side effects and consumed
  randomness are outside the rollback guarantee.

### Documentation

- Replace superseded research and completed plans with a maintained documentation map and product
  roadmap; defer v3 in favor of compatible 2.x delivery.
- Validate repository-local Markdown links and version-policy facts, and document snapshot and
  stream-policy usage across Java, Kotlin, JUnit, and Kotest.

## [2.2.0] - 2026-09-04

### Changed

- Update SLF4J to 2.0.19 and refresh build tooling: Spotless 8.10.1, japicmp 0.26.2,
  and NMCP 1.6.2.
- Update benchmark dependencies to Easy Random 6.0.1, Instancio 6.0.0, and SnakeYAML 2.7.
- Align consumer examples with Kotlin 2.4.10, JUnit 6.1.3, and Spring Boot 4.1.1.
- Refresh immutable GitHub Action pins for Java setup, GraalVM, Pages, and release publication.
- Verify the unchanged public API against the released 2.1.0 baseline without historical exclusions.

### Documentation

- Correct dependency-reproducibility guidance to distinguish version pinning from artifact
  checksum verification, which is not yet enabled for resolved dependencies.
- Refresh public installation coordinates and repository-local examples for 2.2.0.

## [2.1.0] - 2026-08-26

### Added

- Explicit, configuration-scoped `KRandomModule` extensions. A metadata-complete
  `ProviderDescriptor` can now contribute provider aliases, schema projections, safety claims,
  and semantic object-field generation without classpath scanning or global registration;
  conflicts fail during configuration construction.
- Rich contextual generation metadata: `GenerationContext` now exposes the full root-to-field
  path, reflected declared type and declaration, and active `GeneratorConfig`, while retaining
  its existing constructor and field/owner/depth accessors.
- Type-safe Java object rules through `PropertySelector` and composable `PropertyPath`, including
  getter and record-accessor references for root and nested `ObjectFaker` rules.
- Immutable, composable `ObjectModel` fixture configurations and opt-in `ObjectFaker.strict()`
  validation. Existing dependent rules now provide type-safe correlated assignments.
- Critical object-engine mutation testing with measured 85% mutation and 98% mutated-class
  coverage thresholds, plus Gradle and Maven consumer coverage for the new public fixture API.
- `HttpFixtureGenerator`, coherent static HTTP request/response fixture records, and a namespaced
  `network().httpFixture()` entry point. They generate compatible response-body shapes without
  making network calls.
- `PasswordPolicy`, bounded `SequenceGenerator`, and `FinitePoolGenerator` for common test-fixture
  constraints: required password character sets, deterministic nullable sequences, and
  resettable without-replacement pools.
- `SchemaProjection`, which projects existing objects into JSONL, JSON, CSV, XML, SQL, YAML, or
  TOML without materializing a batch. `Schema` now also writes JSON arrays, YAML, and TOML.
- Verified `LocalDataPack` loading and configuration-scoped University fixtures. Packs are local
  directories with a versioned manifest, required provenance/license declarations, bounded
  SHA-256-verified CSV data, and no network-loading path.
- Experimental GraalVM native-image reachability metadata, a conditional local smoke verifier,
  and guidance for registering application-owned reflective types.

## [2.0.0] - 2026-07-17

### Added

- Versioned, human-readable `GenerationRecipe` replay metadata for portable seeded configurations.
- `checkAllWithRecipe` and `krandomKotestRecipe`: Kotest property failures can now carry the
  portable kRandom configuration recipe alongside Kotest's seed report, and the adapter suite is
  verified against the current and previous Kotest minor via `-PkotestVersion`.
- `krandomConfig { }`, an idiomatic Kotlin builder for standalone `GeneratorConfig` values with
  textual seeds and explicit construction policies; the DSL's one intentional default difference
  (`objectOverrideDefaultInitialization`) is now documented and covered by a Java/Kotlin
  equivalence test.
- Spring starter properties for replay and safety: `krandom.recipe` (serialized replay recipe),
  `krandom.clock`/`krandom.clock-zone` (fixed generation clock), the eight `*-safety-policy`
  properties, and `krandom.object-construction-policy`. Invalid combinations fail at context
  startup with actionable messages.
- `@KrandomTest` is now a self-contained Spring test slice: it bootstraps the TestContext
  framework itself, disables full application auto-configuration, and starts only the documented
  kRandom beans; `krandom.*` properties bind as in the full context.
- Type-safe Kotlin DSL rules: `rule(Type::property)` and `exclude(Type::property)` accept
  `KProperty1` references, duplicate field/type rules fail at registration, and unknown field
  rules fail before generation with the known field names.
- `krandomIntArb`, `krandomLongArb`, `krandomDoubleArb`, and `krandomPickArb`, shrinking-aware
  Kotest adapters for bounded primitives and selections with in-range edge cases and
  range-bounded shrink candidates.
- Kotest replay-safe factory and object `Arb` adapters now derive a fresh kRandom configuration
  from each host random-source draw.
- `PaymentCardSafetyPolicy.STRIPE_SANDBOX`, an explicit Stripe-only sandbox mode that maps each
  card type supported by kRandom to Stripe's published interactive test-card number. It requires
  Stripe sandbox/test API keys and is not portable to another processor; server-side Stripe tests
  should use named `PaymentMethod` values instead of raw card numbers.
- `PaymentCardSafetyPolicy`, an enforceable configuration contract for generated card numbers.
- `PhoneNumberSafetyPolicy`, an enforceable contract for locale-style phone-number fixtures.
- `NationalIdSafetyPolicy`, an enforceable fail-closed configuration contract for national-ID
  generation.
- `BankingSafetyPolicy`, an enforceable fail-closed configuration contract for banking identifiers
  and account values.
- `IdentityDocumentSafetyPolicy`, an enforceable fail-closed configuration contract for generic
  passport and driving-license identifiers.
- `BusinessTaxIdentifierSafetyPolicy`, an enforceable fail-closed configuration contract for CNPJ
  and EIN values.
- `CnpjGenerator.withAlphanumericFormat()`, an explicit unclassified compatibility mode for the
  Brazilian 14-character alphanumeric CNPJ shape and its official check-digit algorithm.
- `CryptoAddressSafetyPolicy`, an enforceable fail-closed configuration contract for generated
  cryptocurrency destination-address shapes.
- `SecuritiesIdentifierSafetyPolicy`, an enforceable fail-closed configuration contract for ISIN
  and CUSIP values.
- `ProviderSafetyMetadata`, `ProviderValidity`, and `ProviderTestSafety`, describing conservative
  format, checksum, semantic-plausibility, and test-safety claims from the provider catalog.
- `ProviderSafetyPolicy` and an `x-krandom-safety` JSON Schema extension that carries the selected
  payment-card or phone-number safety policy for classified schema references.
- `ProviderCatalog` definitions now centralize built-in provider keys, aliases, factories,
  schema extractors/metadata, and object-field semantic mappings for `ProviderHub` and
  `FieldLookup`.
- Structured, value-sanitized generation failure context shared by object and schema exceptions, with stable category, operation, path, type, depth, and record-index fields.
- Immutable Kotlin primary-constructor generation in `krandom-kotlin-dsl`, including nullable/non-null parameters, defaults, nested generics, field/type rules, and Jakarta Validation. Java-only core remains free of Kotlin runtime dependencies and reports an actionable integration requirement instead of allocating an invalid Kotlin value.
- An explicit `io.github.frikit.krandom` core module descriptor plus open/closed Java 21 named-module consumers. Missing reflective access now reports the exact qualified `opens` directive.
- Optional `GenerationFailureListener` diagnostics configured through `GeneratorConfig`, exposing only structured context, cause class name, and an optional replay identity—never generated values or throwables.
- `krandom-bom`, a Maven/Gradle platform that keeps all published kRandom modules on one version. Consumer examples now import the BOM and omit individual kRandom module versions.
- Japicmp compatibility and evolution gates check every published jar module against the configured latest-GA baseline (`1.5.0`) in local pre-commit checks, CI, and releases. Binary/source breaks fail independently, while additions or other public changes require exact reviewed classification.
- A generated HTML/XML public API inventory for every published jar module, plus a checked-in
  2.0.0 disposition document covering facade aliases, registries, object generation, and
  integrations.
- Machine-readable release/module/locale/constraint/schema facts with a documentation gate that rejects stale versions, support counts, resource paths, and default-random claims.
- Immutable GitHub Action revisions plus checksum verification for the Gradle wrapper distribution and downloaded Mill launcher, enforced locally and in release CI.
- Strict Gradle dependency verification with reviewed SHA-256 metadata, centralized repositories, and rejection of dynamic or changing dependency selectors.
- Validated CycloneDX 1.6 JSON/XML SBOMs for every published module, attached automatically to GitHub releases.
- Pinned GitHub/Sigstore build-provenance attestations for the signed Maven Central bundle, jars, and SBOMs before publication.

### Changed
- Object generation caches Bean Validation constraint/accessor metadata, reuses sorted time-zone
  metadata, and resolves immutable built-in provider descriptors without rebuilding a mutable
  provider hub for every object.
- Competitor benchmarks separate structural object generation from semantic fixture construction;
  full publishable runs use three forks and report GC/allocation metrics against documented
  regression budgets. Dashboard parsing now handles parameterized and competitor-only JMH tables
  and fails if the generated methodology link is missing.
- Semantic coherence now deterministically derives a birth date from an explicitly overridden age,
  while preserving both values when the user explicitly overrides both fields.
- `PhoneNumberGenerator` now uses NANPA's fictional `555-0100` through `555-0199` range by
  default for US locale-style output. Other locales, custom templates, and MSISDN
  output remain explicitly unclassified; `REALISTIC_UNCLASSIFIED` restores the prior behavior and
  legacy recipes preserve that behavior when their phone-policy setting is absent.
- `CreditCardGenerator` and `CreditCardInfoGenerator` now produce issuer-shaped numbers that
  deliberately fail Luhn by default. `CHECKSUM_VALID` is an explicit validator-fixture opt-in and
  is not a real or processor-sandbox credential; the selected policy is replayed in portable
  generation recipes. Recipes recorded before the policy setting retain their historic
  checksum-valid replay behavior.
- `GeneratorConfig` and `Generators.ofNationalId(...)` now disable national-ID generation by
  default. `REALISTIC_UNCLASSIFIED` is an explicit compatibility opt-in; the locale and seeded
  `NationalIdGenerator` constructors are removed, and portable recipes persist the selected
  policy.
- `GeneratorConfig` and canonical banking facades now disable account numbers, ABA routing
  numbers, BBANs, IBANs, BICs, and structured bank payloads by default.
  `REALISTIC_UNCLASSIFIED` is an explicit compatibility opt-in; the affected direct constructors
  are removed. Payment payloads use an opaque `ACCT-TEST-####` reference for bank methods while
  banking output is disabled, and portable recipes persist the selected policy.
- `GeneratorConfig`, `Generators.ofPassport()`, and `Generators.ofDrivingLicense()` now disable
  generic document identifiers by default. `REALISTIC_UNCLASSIFIED` is an explicit compatibility
  opt-in; the direct `PassportGenerator` and `DrivingLicenseGenerator` constructors are removed,
  and portable recipes persist the selected policy.
- `GeneratorConfig`, `Generators.ofCnpj()`, and `Generators.ofEin()` now disable corporate tax-ID
  generation by default. `REALISTIC_UNCLASSIFIED` is an explicit compatibility opt-in; direct
  `CnpjGenerator` and `EinGenerator` constructors are removed, and portable recipes persist the
  selected policy.
- `GeneratorConfig` and canonical crypto-address facades now disable plausible destination-address
  output by default. `REALISTIC_UNCLASSIFIED` is an explicit compatibility opt-in; the direct
  `CryptoAddressGenerator` constructor is removed, and portable recipes persist the selected
  policy.
- `GeneratorConfig` and canonical ISIN/CUSIP facades now disable securities-identifier output by
  default. `REALISTIC_UNCLASSIFIED` is an explicit compatibility opt-in; direct `IsinGenerator`
  and `CusipGenerator` constructors are removed, and portable recipes persist the selected
  policy.
- Recursive object generation now distinguishes resolved generic signatures when detecting cycles
  and reusing completed objects, so nested Kotlin generic data classes retain their concrete type
  bindings instead of receiving a value from a different erased generic instantiation.
- Data-registry customisation is now configuration-scoped. Use the matching
  `DataRegistryContext.Builder` registration method to isolate custom locale data to one
  `GeneratorConfig`.
- The Java + Gradle consumer example now demonstrates a custom weather provider scoped to one
  `GeneratorConfig`, rather than process-wide static registry mutation.
- `WeatherGenerator` now resolves vocabulary through `DataRegistryContext`, so independent
  `GeneratorConfig` instances can safely use different validated weather providers for the same
  locale. The global weather registry remains a compatibility fallback.
- `MeasurementGenerator` now resolves unit vocabulary through `DataRegistryContext`, so a custom
  provider is isolated to its owning `GeneratorConfig`; the global registry remains a fallback.
- `FinancialTermGenerator` now resolves vocabulary through `DataRegistryContext`, so a custom
  provider is isolated to its owning `GeneratorConfig`; the global registry remains a fallback.
- `RestaurantTypeGenerator` now resolves vocabulary through `DataRegistryContext`, so a custom
  provider is isolated to its owning `GeneratorConfig`; the global registry remains a fallback.
- `HobbyGenerator` now resolves vocabulary through `DataRegistryContext`, so a custom provider is
  isolated to its owning `GeneratorConfig`; the global registry remains a fallback.
- `NationalityGenerator` now resolves vocabulary through `DataRegistryContext`, so a custom
  provider is isolated to its owning `GeneratorConfig`; the global registry remains a fallback.
- `PronounGenerator` now resolves vocabulary through `DataRegistryContext`, so a custom provider
  is isolated to its owning `GeneratorConfig`; context registration now rejects malformed
  `subject/object` values; the global registry remains a fallback.
- `BloodTypeGenerator` now resolves distributions through `DataRegistryContext`, so custom
  parallel positive-weight distributions are isolated to their owning `GeneratorConfig`; the global
  registry remains a fallback.
- `ChineseZodiacGenerator` now resolves its validated, ordered twelve-animal cycle through
  `DataRegistryContext`, so per-configuration vocabulary preserves year-to-animal mapping; the
  global registry remains a fallback.
- `ZodiacGenerator` now resolves its validated, ordered twelve-sign cycle through
  `DataRegistryContext`, so per-configuration vocabulary preserves date-to-sign mapping; the
  global registry remains a fallback.
- `krandom-core` is now a Java-only build and no longer publishes an unused `kotlin-stdlib` runtime dependency. Kotlin remains confined to the Kotlin DSL and Kotest integration modules.
- Maven Central publication now uses NMCP's explicit aggregation plugin and an exact seven-module graph, removing the convenience settings plugin's Gradle 10 deprecation.

### Changed (breaking)
- Bounded generators enforce strict bound semantics: `min >= max` now throws
  `IllegalArgumentException` with one consistent message across int, long, double, float, short,
  byte, prime, number, and atomic generators. Reversed bounds were previously swapped silently;
  2.0.0 never swaps a caller mistake. The protected `lo`/`hi` helpers on
  `AbstractBoundedGenerator` are removed.

### Removed
- The legacy 1.x `Generators` facade aliases `constant`, `pickFrom`, `pickset`,
  `pickSetFrom`, `shuffleOf`, and `uniqueValues`. Use the canonical `ofConstant`, `pick`,
  `pickSet`, `shuffle`, and `unique`; the migration table is in `docs/migration/v1.6-to-v2.md`.
- `Generator.reseed(long)` and `Generator.reseed(String)`, including the reflection-based
  fallback that mutated discovered `Random` fields. Reseeding is now exclusively the typed
  `Seedable` contract; see the "Typed reseeding" migration section.
- The mutable Kotest bridges `Generator.toArb()`, the no-argument `krandomArb { ... }` factory,
  and `krandomObjectArb`. Use `krandomArb(config) { ... }`, `krandomReplayObjectArb`, and the
  shrinking-aware bounded arbs, which derive fresh generators from Kotest's `RandomSource`.
- All process-wide registry mutation: the 23 legacy static `register(...)`/`append(...)`
  methods on the data registries and `LocaleDataBundle.registerGlobal()`. Register custom
  vocabulary on the consuming configuration via `DataRegistryContext.builder()` instead; built-in
  locale data still loads automatically and read-only lookups are unchanged.
- The 21 legacy no-argument and `Locale`-based constructors on the finance and identity
  generators (ABA routing, bank account/info, BBAN, BIC, IBAN, ISIN, CUSIP, EIN, CNPJ, crypto
  address, passport, driving license, national ID). Construct them with a `GeneratorConfig`
  carrying an explicit safety policy; the removed bridges' exact replacement expressions are in
  `docs/migration/v1.6-to-v2.md`.

### Fixed
- Recursive object generation now retains type-use annotations for optional values, array
  components, collection elements, and map keys/values. Nested Bean Validation constraints apply
  at the corresponding type node and failures retain the composed child path.
- `Generator.map(...)` and `Generator.filter(...)` now preserve deterministic reseeding when their source implements `Seedable`; non-seedable sources remain honest and do not claim that capability.
- Object generation no longer swallows custom-map insertion failures and returns a partial map: strict mode reports sanitized indexed context, while explicit lenient mode discards the whole map.
- Concrete list, set, and queue insertion failures no longer return partial or unexplained values: strict mode reports sanitized field context, while explicit lenient mode discards the whole collection.
- Concrete collection and map constructors that throw now produce sanitized construction context in strict mode and `null` in explicit lenient mode; types without a no-arg constructor retain the compatibility fallback.
- Primitive-array element assignment now fails with sanitized indexed context in strict mode; explicit lenient mode retains the documented JVM default element.
- Direct fields with unsupported interface, abstract, `Object`, or JDK types now fail with sanitized structured context by default; explicit lenient mode retains the type-default fallback.
- `@Randomizer` construction and execution failures now report the sanitized owner field path, operation, and generator type while preserving original or already-structured causes.
- Strict nested-object failures now compose one root-relative path across every object boundary while retaining the child failure category, operation, owner type, depth, and original cause.
- Top-level class and record constructor failures now expose sanitized `CONSTRUCTION` context and preserve the original target exception without copying its message.
- Semantic-coherence reflection failures now expose structured read/alignment context; explicit lenient mode returns `null` for failed reads or retains the prior value for failed writes and emits value-sanitized diagnostics.
- Object-generation assignment, container, nested, unsupported-type, and semantic-reflection failures now share one strict/lenient policy with standardized value-sanitized diagnostics and unchanged documented fallbacks.
- `ObjectFaker` nested rule, include, and ignore reflection failures now report sanitized root-relative target paths and preserve the original cause.
- Schema value-provider and metadata-export failures now have distinct operations and messages; metadata export no longer reports a synthetic record index.
- Schema semantic resolution now probes reference membership explicitly instead of swallowing arbitrary runtime failures during binding.
- Schema record conversion now reports sanitized nested component paths and types while preserving the unwrapped accessor failure.
- Object generation now preserves nested parameterized types through optionals, collections, maps, mutable fields, and Java record components.
- Raw containers and unbounded generic arguments now fail contextually by default instead of producing null-filled values; upper/lower wildcard bounds generate their effective type.
- Concrete generic superclass/interface bindings now propagate into inherited direct fields and container elements, including custom collection subtypes.
- Generic arrays now preserve bound and parameterized component types for mutable fields and records; unresolved components fail at the parent field instead of producing null-filled arrays.
- Parameterized nested classes and records now retain their type-variable bindings through direct fields, containers, and inherited declarations; raw or unbounded child types fail at the parent boundary.
- Record JSON Schema inference now retains nested collections, maps, optionals, generic records, generic arrays, wildcard bounds, enums, and fixed generic collection subtypes; Optional values serialize as their contained value or `null`.
- Bean Validation nullability and size rules now normalize `@Null`, `@NotNull`, `@NotEmpty`, `@NotBlank`, and `@Size`; required constraints override null/empty probabilities, while impossible intersections and unsupported targets fail contextually.
- Numeric, sign, assertion, and temporal Bean Validation constraints now intersect before generation; empty real or target-type domains, contradictory assertions/directions, malformed bounds, and unsupported scalar targets fail through the structured generation policy.
- String Bean Validation now composes email, repeatable pattern, numeric, size, and blankness rules through a bounded candidate search; incompatible combinations, malformed or unsupported regex syntax, and invalid targets fail contextually.
- The published Bean Validation guide now derives its 21-constraint matrix from repository facts, with deterministic Hibernate Validator coverage across fields, getters, records, and interface accessors.
- Object generation now defaults to `SAFE_CONSTRUCTORS`: it invokes no-argument or one unambiguous declared constructor with generated arguments; legacy Objenesis allocation requires explicit `UNSAFE_CONSTRUCTOR_BYPASS` configuration.
- Safe construction now rejects abstract, interface, array, primitive, enum, annotation, local, anonymous, and non-static inner roots before allocation; constructor parameters use the same type and Bean Validation resolver as fields.
- Type and contextual object overrides now act as validated root factories before reflection, including for interfaces; null, wrong-type, and throwing factories use structured strict/lenient custom-generator handling, and field-access preflight runs before constructors.
- Core locale and provider datasets now load through their owning classes so resources remain available when `krandom-core` runs as a strongly encapsulated named module.

## [1.5.0] - 2026-06-22

### Added
- **21 new DataFaker-parity generators**, each exposed on the `Generators` facade with 100% line/branch coverage. The locale-aware vocabulary generators ship per-locale resource files for all 35 built-in locales (`krandom/<concept>/<locale>.txt`) with English fallback.
  - **Person attributes:** `BloodTypeGenerator` (`ofBloodType`, locale-weighted ABO/Rh), `ZodiacGenerator` (`ofZodiac`, Western signs + `signFor(date)`; localized across 35 locales), `ChineseZodiacGenerator` (`ofChineseZodiac`, animals + `animalFor(year)`; 35 locales), `PronounGenerator` (`ofPronoun`, subject/object sets; 35 locales), `MbtiGenerator` (`ofMbti`, 16 types + `withNickname()`), `NationalityGenerator` (`ofNationality`, demonyms; 35 locales), `HobbyGenerator` (`ofHobby`; 35 locales).
  - **Localized vocabulary (35 locales):** `MeasurementGenerator` (`ofMeasurement`, units), `FinancialTermGenerator` (`ofFinancialTerm`), `RestaurantTypeGenerator` (`ofRestaurantType`, cuisine/type), `WeatherGenerator` (`ofWeather`, conditions).
  - **Identifiers & formats:** `VinGenerator` (`ofVin`, ISO-3779 check digit) + `VehicleGenerator` (`ofVehicle`, make/model/plate); `CnpjGenerator` (`ofCnpj`) and `ofCpf()` for Brazilian company/person tax ids (check-digit valid); `PassportGenerator` (`ofPassport`); `DrivingLicenseGenerator` (`ofDrivingLicense`).
  - **Technical / universal:** `NatoPhoneticGenerator` (`ofNatoPhonetic`, ICAO; `wordFor`/`spell`), `ProgrammingLanguageGenerator` (`ofProgrammingLanguage`), `AwsGenerator` (`ofAws`; region/instanceId/s3Bucket), `AzureGenerator` (`ofAzure`; region/resourceGroup), `ComputerGenerator` (`ofComputer`; OS/platform/deviceType).
- `LocaleTextResourceLoader` is now `public` so locale-aware generators in any package can reuse the shared classpath resource loader.
- A source-audited DataFaker provider mapping, competitive gap tracker, Instancio parity matrix,
  and migration guides from JavaFaker, DataFaker, Easy Random, and Instancio. The dated research
  snapshots remain available in the `v2.1.0` tag; the migration guides remain maintained.

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

The original review context remains available in Git history and the `v1.0.0` tag.

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

[Unreleased]: https://github.com/frikit/krandom/compare/v2.4.0...HEAD
[2.4.0]: https://github.com/frikit/krandom/compare/v2.3.0...v2.4.0
[2.3.0]: https://github.com/frikit/krandom/compare/v2.2.0...v2.3.0
[2.2.0]: https://github.com/frikit/krandom/compare/v2.1.0...v2.2.0
[2.1.0]: https://github.com/frikit/krandom/compare/v2.0.0...v2.1.0
[2.0.0]: https://github.com/frikit/krandom/compare/v1.5.0...v2.0.0
[1.5.0]: https://github.com/frikit/krandom/compare/v1.4.0...v1.5.0
[1.4.0]: https://github.com/frikit/krandom/compare/v1.3.0...v1.4.0
[1.3.0]: https://github.com/frikit/krandom/compare/v1.2.0...v1.3.0
[1.2.0]: https://github.com/frikit/krandom/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/frikit/krandom/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/frikit/krandom/releases/tag/v1.0.0
