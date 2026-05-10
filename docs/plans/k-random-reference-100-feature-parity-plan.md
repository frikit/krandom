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

Goal: provide equivalent extension points without cloning the reference SPI.

- [ ] Map reference `Randomizer<T>` to local `Generator<T>`.
- [ ] Map reference `ContextAwareRandomizer<T>` to local `ContextualGenerator<T>`.
- [ ] Map reference field/type randomizer registration to `GeneratorConfig.objectOverride(...)`.
- [ ] Map reference custom registries/providers to native `ProviderHub`, object overrides, and generator composition.
- [ ] Decide whether any missing extension hook needs a native feature:
  - [ ] object factory hook
  - [ ] exclusion policy hook
  - [ ] registry priority model
  - [ ] ServiceLoader discovery
- [ ] Prefer explicit native extension APIs over ServiceLoader unless product demand requires dynamic plugin discovery.

Acceptance:

- Migration guide has "Custom Randomizers And Registries" replacements.
- Any intentionally omitted SPI has a documented native alternative.

## Phase 5: Built-In Randomizer Capability Parity

Goal: ensure every reference built-in randomizer family has a native generator equivalent.

- [ ] Primitive and wrapper types.
- [ ] Numbers and ranges.
- [ ] BigInteger and BigDecimal.
- [ ] AtomicInteger and AtomicLong.
- [ ] Collections and maps.
- [ ] Optional.
- [ ] UUID and locale.
- [ ] URI and URL.
- [ ] Time and date types:
  - [ ] `java.util.Date`
  - [ ] `java.sql.Date`
  - [ ] `java.sql.Time`
  - [ ] `java.sql.Timestamp`
  - [ ] `LocalDate`
  - [ ] `LocalTime`
  - [ ] `LocalDateTime`
  - [ ] `Instant`
  - [ ] `OffsetDateTime`
  - [ ] `OffsetTime`
  - [ ] `ZonedDateTime`
  - [ ] `Year`
  - [ ] `YearMonth`
  - [ ] `MonthDay`
  - [ ] `Duration`
  - [ ] `Period`
  - [ ] `ZoneId`
  - [ ] `ZoneOffset`
  - [ ] `TimeZone`
- [ ] Text/string/regex generation.
- [ ] Faker-style domain data:
  - [ ] names
  - [ ] email/password/phone
  - [ ] city/state/country/street/postal code
  - [ ] company
  - [ ] credit card
  - [ ] ISBN
  - [ ] IPv4/IPv6/MAC
  - [ ] latitude/longitude
  - [ ] word/sentence/paragraph

Acceptance:

- The audit matrix shows a native generator for each reference randomizer class or a documented migration replacement.

## Phase 6: Bean Validation Feature Parity

Goal: match reference Bean Validation behavior in native object generation.

- [ ] Support or verify:
  - [ ] `AssertFalse`
  - [ ] `AssertTrue`
  - [ ] `DecimalMin`
  - [ ] `DecimalMax`
  - [ ] `Email`
  - [ ] `Future`
  - [ ] `FutureOrPresent`
  - [ ] `Max`
  - [ ] `Min`
  - [ ] `Negative`
  - [ ] `NegativeOrZero`
  - [ ] `NotBlank`
  - [ ] `Null`
  - [ ] `Past`
  - [ ] `PastOrPresent`
  - [ ] `Pattern`
  - [ ] `Positive`
  - [ ] `PositiveOrZero`
  - [ ] `Size`
- [ ] Support `@Size` for strings, collections, lists, sets, maps, and arrays.
- [ ] Support field annotations and getter/method annotations where the reference does.
- [ ] Add validator integration tests with Hibernate Validator.
- [ ] Document migration from `k-random-bean-validation` to native krandom core behavior.

Acceptance:

- Generated objects satisfy supported Bean Validation constraints through native APIs.

## Phase 7: Determinism And Compatibility Guarantees

Goal: make the migration promise precise.

- [ ] Document that krandom guarantees deterministic local output for seeded generators.
- [ ] Document that exact k-random/DataFaker output strings are not guaranteed unless explicitly added.
- [ ] Add repeatability tests for migrated examples.
- [ ] Add seed migration notes where default seeds differ.

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
