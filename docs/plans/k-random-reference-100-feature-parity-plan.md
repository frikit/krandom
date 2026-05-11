# k-random Reference 100% Native Feature Parity Plan

## Goal

Achieve 100% practical feature parity with the reference project without bringing the reference API, package names, modules, or artifacts into this codebase.

- Reference repository: https://github.com/k-random/k-random
- Reviewed commit: `43d5b6f4ea38b59ce73c90d9f47e3b25e9c57f32`
- Reference modules: `core`, `randomizers`, `bean-validation`
- Reference package surface: `io.github.krandom.*`
- Local target surface: existing `io.github.frikit.krandom.*` APIs

This is **not** a drop-in compatibility plan. We will not add `io.github.krandom` packages, `k-random-*` modules, or reference artifact coordinates. The outcome should be:

- This library has equivalent or better capabilities for every reference feature.
- Users can migrate from k-random to this library using documented API mappings and examples.
- Any reference feature that is intentionally not copied as an API is represented by a native krandom alternative.

## Parity Definition

100% native feature parity means:

- Every reference capability has a tested local equivalent or a documented intentional non-goal.
- Behavioral guarantees are covered with local APIs, not reference packages.
- Migration docs explain how to translate common reference usage into this library.
- Deterministic seeding and validity guarantees are documented where the reference relies on them.
- No compatibility/facade modules are added solely to compile old imports.

Exact byte-for-byte generated fake data is out of scope unless promoted by a separate product decision. The target is equivalent capability and deterministic local behavior, not cloning upstream output strings.

## Source Of Truth

- Audit: `docs/feature-parity/k-random-reference-parity.md`
- Reference source: `/private/tmp/k-random-reference`
- Local implementation areas:
  - `core/src/main/java/io/github/frikit/krandom/generator`
  - `core/src/main/java/io/github/frikit/krandom/generator/object`
  - `core/src/main/java/io/github/frikit/krandom/generator/provider`
  - integration modules under `jackson`, `spring-boot-starter`, `jqwik-extensions`, `kotest-extensions`, and `kotlin-dsl`

## Delivery Rules

For every phase:

1. Add local tests first using `io.github.frikit.krandom.*` APIs.
2. Use reference tests as behavioral inspiration, not as source-compatible compile targets.
3. Implement capabilities in existing modules unless a native optional module is justified.
4. Preserve current public APIs unless a breaking change is explicitly approved.
5. Update the audit, this plan, and migration guide together.
6. Run narrow relevant tests, then `./scripts/pre_commit_check.sh` before a phase is considered complete.

## Phase 0: Scope Correction And Mapping Baseline

Status: **Completed**.

Goal: lock the non-drop-in strategy before more code is added.

- [x] Confirm no `io.github.krandom.*` compatibility packages will be added.
- [x] Confirm no `k-random-core`, `k-random-randomizers`, or `k-random-bean-validation` modules will be added.
- [x] Keep local APIs as the target surface.
- [x] Generate a reference feature inventory from the cloned repo in `docs/feature-parity/k-random-reference-feature-inventory.md`.
- [x] Map each reference feature to:
  - [x] existing krandom API
  - [x] missing krandom feature
  - [x] migration-doc-only replacement
  - [x] intentional non-goal
- [x] Create the migration guide shell at `docs/migration/k-random-to-krandom.md`.

Acceptance:

- The plan no longer asks for reference packages/modules.
- The first migration guide draft exists and lists top-level API mappings.

## Phase 1: Native Object Generation Parity

Status: **Completed**.

Goal: match k-random's object-generation capabilities through krandom's `ObjectGenerator`, `ObjectFaker`, and `GeneratorConfig`.

- [x] Add tests covering migration equivalents for:
  - [x] `new KRandom().nextObject(MyType.class)` -> `Generators.ofObject(MyType.class).generate()`
  - [x] `objects(type, size)` -> `generateList(size)` / stream equivalents
  - [x] seeded object generation
  - [x] records
  - [x] nested objects
  - [x] arrays
  - [x] collections
  - [x] maps
  - [x] optionals
  - [x] circular references
  - [x] object pool behavior
  - [x] max depth behavior
- [x] Fill any missing behavior in native object generation. No code gaps were exposed by the Phase 1 parity tests.
- [x] Document migration examples for basic object generation.

Acceptance:

- A k-random user can generate equivalent object graphs using documented krandom APIs.
- Object-generation migration examples compile in existing examples or tests.

## Phase 2: Native Configuration Mapping

Status: **Completed**.

Goal: provide native equivalents for `KRandomParameters`.

- [x] Map `seed(long)` to `GeneratorConfig.builder().seed(long)`.
- [x] Map `charset(...)` to `GeneratorConfig.builder().charset(...)`.
- [x] Map `stringLengthRange(min, max)` to `stringLength(min, max)`.
- [x] Map `collectionSizeRange(min, max)` to `collectionSize(min, max)`.
- [x] Map `objectPoolSize(...)` to `objectPoolSize(...)`.
- [x] Map `randomizationDepth(...)` to `objectMaxDepth(...)`.
- [x] Map `dateRange(...)` to object/date generator ranges.
- [x] Map `ignoreRandomizationErrors(...)` to `objectIgnoreErrors(...)`.
- [x] Map `overrideDefaultInitialization(...)` to `objectOverrideDefaultInitialization(...)`.
- [x] Decide native handling for `bypassSetters(...)`: document direct-field population as the native replacement.
- [x] Decide native handling for `scanClasspathForConcreteTypes(...)`: document explicit override/provider replacement.

Decision:

- `bypassSetters(true)` maps to krandom's existing direct-field object population. k-random's setter-first default is not copied as a native mode in this phase.
- `scanClasspathForConcreteTypes(true)` maps to explicit `objectOverride(...)` registrations for abstract/interface fields. Native classpath scanning is not added in this phase.

Acceptance:

- Migration guide includes a `KRandomParameters` mapping table.
- Tests prove native config behavior for every mapped setting.

## Phase 3: Exclusions And Declarative Rules

Status: **Completed**.

Goal: provide native equivalents for reference field/type exclusion and annotation-driven randomization.

- [x] Verify current `FieldPredicates` and `TypePredicates` local APIs cover:
  - [x] name matching
  - [x] type matching
  - [x] declaring class matching
  - [x] annotation matching
  - [x] modifier matching
  - [x] predicate composition
- [x] Verify or add local exclusion tests for nested paths and inherited fields.
- [x] Map reference `@Exclude` to local `io.github.frikit.krandom.generator.object.Exclude`.
- [x] Map reference `@Randomizer` and `@RandomizerArgument` to local annotations.
- [x] Ensure constructor argument conversion coverage matches reference behavior where useful.
- [x] Document migration examples for excludes, custom randomizers, and field/type overrides.

Decision:

- `FieldPredicates.named(...)` remains exact-match in krandom; regex migration uses `FieldPredicates.nameMatches(...)`.
- `TypePredicates` now includes native helpers for named, exact type, package, annotation, modifier, interface, abstract, enum, array, and assignability checks.
- `@RandomizerArgument` conversion supports common primitive/wrapper values, enums, big numbers, Java/SQL date-time values, Java time values, and arrays.

Acceptance:

- Exclusion and annotation features have local tests and migration examples.

## Phase 4: Extension Model Equivalents

Status: **Completed**.

Goal: provide equivalent extension points without cloning the reference SPI.

- [x] Map reference `Randomizer<T>` to local `Generator<T>`.
- [x] Map reference `ContextAwareRandomizer<T>` to local `ContextualGenerator<T>`.
- [x] Map reference field/type randomizer registration to `GeneratorConfig.objectOverride(...)`.
- [x] Map reference custom registries/providers to native `ProviderHub`, object overrides, and generator composition.
- [x] Decide whether any missing extension hook needs a native feature:
  - [x] object factory hook
  - [x] exclusion policy hook
  - [x] registry priority model
  - [x] ServiceLoader discovery
- [x] Prefer explicit native extension APIs over ServiceLoader unless product demand requires dynamic plugin discovery.

Decision:

- k-random `Randomizer<T>.getRandomValue()` maps to native `Generator<T>.generate()`.
- k-random `ContextAwareRandomizer<T>` maps to native `ContextualGenerator<T>`, with `GenerationContext` exposing field name, owner type, and depth. Full root-object/current-object/path context is not copied into the native API.
- Predicate-based field randomizers are now supported natively through `GeneratorConfig.builder().objectOverride(Predicate<Field>, Generator<T>)` and the contextual overload.
- Registry/provider patterns map to explicit object overrides, direct generator composition, `ProviderHub.register(...)`, aliases, and `ConflictPolicy`.
- A public `ObjectFactory` hook is not added. Native construction uses constructors plus Objenesis fallback; factory-like special cases should use explicit type/field overrides.
- Registry priority annotations and ServiceLoader discovery remain intentional non-goals for the native API. Use explicit registration order and `ConflictPolicy` instead.

Acceptance:

- Migration guide has "Custom Randomizers And Registries" replacements.
- Any intentionally omitted SPI has a documented native alternative.

## Phase 5: Built-In Randomizer Capability Parity

Status: **Completed**.

Goal: ensure every reference built-in randomizer family has a native generator equivalent.

- [x] Primitive and wrapper types.
- [x] Numbers and ranges.
- [x] BigInteger and BigDecimal.
- [x] AtomicInteger and AtomicLong.
- [x] Collections and maps.
- [x] Optional.
- [x] UUID and locale.
- [x] URI and URL.
- [x] Time and date types:
  - [x] `java.util.Date`
  - [x] `java.sql.Date`
  - [x] `java.sql.Time`
  - [x] `java.sql.Timestamp`
  - [x] `LocalDate`
  - [x] `LocalTime`
  - [x] `LocalDateTime`
  - [x] `Instant`
  - [x] `OffsetDateTime`
  - [x] `OffsetTime`
  - [x] `ZonedDateTime`
  - [x] `Year`
  - [x] `YearMonth`
  - [x] `MonthDay`
  - [x] `Duration`
  - [x] `Period`
  - [x] `ZoneId`
  - [x] `ZoneOffset`
  - [x] `TimeZone`
- [x] Text/string/regex generation.
- [x] Faker-style domain data:
  - [x] names
  - [x] email/password/phone
  - [x] city/state/country/street/postal code
  - [x] company
  - [x] credit card
  - [x] ISBN
  - [x] IPv4/IPv6/MAC
  - [x] latitude/longitude
  - [x] word/sentence/paragraph

Decision:

- Standalone atomic and `Number` randomizers now map to native `AtomicIntegerGenerator`, `AtomicLongGenerator`, and `NumberGenerator`.
- Standalone missing time randomizers now map to native `OffsetTimeGenerator`, `YearGenerator`, `YearMonthGenerator`, `MonthDayGenerator`, `PeriodGenerator`, `ZoneIdGenerator`, `ZoneOffsetGenerator`, and `LegacyTimeZoneGenerator`.
- Existing `TimezoneGenerator` remains the string timezone-id generator. The legacy `java.util.TimeZone` generator intentionally uses a distinct class name to avoid case-insensitive filesystem collisions.
- Reference `URI` and `URL` randomizers map to typed `Generators.ofURI()` and `Generators.ofURL()` when Java objects are needed, or to existing string `ofUri()` and `ofUrl()` generators for URL-form strings.
- Standalone collection/map/optional randomizer classes migrate to native generator composition: `generateList`, `repeat`, `pick`, object field population, and `objectOptionalEmptyProbability(...)`.
- Exact reference/DataFaker seeded output strings remain out of scope; local deterministic behavior is covered by native seeds.

Acceptance:

- The audit matrix shows a native generator for each reference randomizer class or a documented migration replacement.

## Phase 6: Bean Validation Feature Parity

Status: **Completed**.

Goal: match reference Bean Validation behavior in native object generation.

- [x] Support or verify:
  - [x] `AssertFalse`
  - [x] `AssertTrue`
  - [x] `DecimalMin`
  - [x] `DecimalMax`
  - [x] `Email`
  - [x] `Future`
  - [x] `FutureOrPresent`
  - [x] `Max`
  - [x] `Min`
  - [x] `Negative`
  - [x] `NegativeOrZero`
  - [x] `NotBlank`
  - [x] `Null`
  - [x] `Past`
  - [x] `PastOrPresent`
  - [x] `Pattern`
  - [x] `Positive`
  - [x] `PositiveOrZero`
  - [x] `Size`
- [x] Support `@Size` for strings, collections, lists, sets, maps, queues, and arrays.
- [x] Support field annotations and getter/method annotations where the reference does.
- [x] Add validator integration tests with Hibernate Validator.
- [x] Document migration from `k-random-bean-validation` to native krandom core behavior.

Decision:

- Bean Validation support remains native to `krandom-core`; no `k-random-bean-validation` compatibility module is added.
- Getter/method constraints are resolved from JavaBean accessors, boolean accessors, record accessors, and interface accessor declarations.
- Numeric constraints now cover primitive/wrapper byte, short, int, long, float, double, `Number`, `BigInteger`, `BigDecimal`, and numeric strings where Bean Validation supports numeric text.
- Temporal constraints now cover common Java temporal targets used by the reference: `Date`, `Calendar`, SQL date/timestamp, `Instant`, `LocalDate`, `LocalDateTime`, `LocalTime`, `OffsetDateTime`, `OffsetTime`, `Year`, `YearMonth`, `MonthDay`, and `ZonedDateTime`.

Acceptance:

- Generated objects satisfy supported Bean Validation constraints through native APIs.

## Phase 7: Determinism And Compatibility Guarantees

Status: **Completed**.

Goal: make the migration promise precise.

- [x] Document that krandom guarantees deterministic local output for seeded generators.
- [x] Document that exact k-random/DataFaker output strings are not guaranteed unless explicitly added.
- [x] Add repeatability tests for migrated examples.
- [x] Add seed migration notes where default seeds differ.

Decision:

- A krandom seed is a krandom repeatability contract: the same krandom version, config, locale, and generator path should repeat the same local sequence.
- A k-random seed is not an output compatibility contract. Migrating `seed(123L)` to `GeneratorConfig.builder().seed(123L)` preserves explicit seeding as a concept, not exact strings or object snapshots from k-random/DataFaker.
- k-random's default seed `123`, string range `1..32`, collection range `1..100`, and effectively unlimited object depth should be set explicitly in krandom only when users relied on those defaults.

Acceptance:

- Migration guide has a determinism section and examples show seeded repeatability.

## Phase 8: Migration Guide And Examples

Goal: make k-random users successful without source-compatible imports.

- [ ] Create `docs/migration/k-random-to-krandom.md`.
- [ ] Include install coordinates for this library.
- [ ] Add mapping tables:
  - [ ] `KRandom`
  - [ ] `KRandomParameters`
  - [ ] randomizer classes
  - [ ] annotations
  - [ ] extension points
  - [ ] Bean Validation
- [ ] Add before/after examples:
  - [ ] basic object generation
  - [ ] seeded generation
  - [ ] field override
  - [ ] type override
  - [ ] exclusion
  - [ ] Bean Validation
  - [ ] faker/domain generators
- [ ] Link migration guide from `README.md`, docs site, and docs index.

Acceptance:

- A user can migrate representative k-random usage by following the guide.
- Examples compile in a local verification task.

## Initial Implementation Order

1. Phase 0 mapping baseline and migration guide shell.
2. Phase 1 object-generation parity tests using native APIs.
3. Phase 2 configuration mapping.
4. Phase 6 Bean Validation gaps.
5. Phase 5 randomizer capability matrix.
6. Phase 3 exclusions and declarative rules.
7. Phase 4 extension model equivalents.
8. Phase 7 determinism documentation.
9. Phase 8 final migration guide and examples.

## Completion Gate

The plan is complete when:

- No reference compatibility modules/packages have been added.
- Every reference feature is implemented natively, mapped to an existing local API, or explicitly rejected with rationale.
- Migration guide covers the main source-level changes a k-random user must make.
- Native tests cover the parity behavior.
- `./scripts/pre_commit_check.sh` passes.
