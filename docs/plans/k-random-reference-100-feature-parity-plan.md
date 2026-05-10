# k-random Reference 100% Feature Parity Plan

## Goal

Achieve defensible 100% feature parity with the reference project:

- Repository: https://github.com/k-random/k-random
- Reviewed commit: `43d5b6f4ea38b59ce73c90d9f47e3b25e9c57f32`
- Modules: `core`, `randomizers`, `bean-validation`
- Package surface: `io.github.krandom.*`

This plan targets strict parity, not only "similar capability". A user should be able to port representative reference code and tests to this project with minimal or no source changes once the compatibility modules are enabled.

## Parity Definition

100% parity means all of the following are either implemented and tested, or explicitly documented as intentionally out of scope with a product decision:

- Source/API compatibility for public reference classes, annotations, interfaces, and constructor/method overloads.
- Behavioral compatibility for object generation, randomizer lookup, exclusions, extension hooks, collection handling, Bean Validation, and error behavior.
- Module/build compatibility for reference-equivalent artifacts or compatibility modules.
- Deterministic seeding compatibility where the reference documents or tests seeded behavior.
- Documentation and examples that compile against the compatibility API.

Exact byte-for-byte generated fake data is a separate decision. If exact seeded approval output parity is required, the plan must pin DataFaker behavior and copy the reference seeding path in compatibility randomizers.

## Source Of Truth

- Audit: `docs/feature-parity/k-random-reference-parity.md`
- Reference source: `/private/tmp/k-random-reference`
- Local object-generation core:
  - `core/src/main/java/io/github/frikit/krandom/generator/object`
  - `core/src/main/java/io/github/frikit/krandom/generator/GeneratorConfig.java`
- Compatibility tests should import reference fixtures and representative upstream tests before implementation work starts.

## Delivery Rules

For every phase:

1. Add compatibility tests first using `io.github.krandom.*` imports.
2. Implement the smallest vertical slice that makes those tests pass.
3. Preserve current first-party APIs unless a breaking change is explicitly approved.
4. Update the audit and this plan checklist.
5. Run the narrow relevant Gradle tests, then `./scripts/pre_commit_check.sh` before a phase is considered complete.

## Phase 0: Contract Freeze And Test Harness

Goal: prevent vague parity claims by locking the target surface.

- [ ] Record the reference commit, latest release tag, Gradle modules, and Maven coordinates in the audit.
- [ ] Generate a public API inventory from the reference packages:
  - [ ] `io.github.krandom`
  - [ ] `io.github.krandom.api`
  - [ ] `io.github.krandom.annotation`
  - [ ] `io.github.krandom.randomizers.*`
  - [ ] `io.github.krandom.validation`
- [ ] Import a minimal reference fixture set under compatibility tests.
- [ ] Add a `k-random-compat` test suite or module that compiles only against `io.github.krandom.*`.
- [ ] Decide whether exact seeded approval outputs are in scope.

Acceptance:

- There is a generated API checklist committed to docs or tests.
- At least one failing compatibility test proves the new harness catches missing `KRandom` API.

## Phase 1: Compatibility Module Skeleton

Goal: establish the package and artifact shape without changing existing APIs.

- [ ] Add compatibility modules or source sets:
  - [ ] `k-random-core` equivalent
  - [ ] `k-random-randomizers` equivalent
  - [ ] `k-random-bean-validation` equivalent
- [ ] Add package roots for `io.github.krandom.*`.
- [ ] Wire publication metadata or internal project names so coordinates can be mapped cleanly.
- [ ] Add `META-INF/services/io.github.krandom.api.RandomizerRegistry` resource locations.
- [ ] Ensure no package collision with existing `io.github.frikit.krandom.*` APIs.

Acceptance:

- Compatibility modules compile with empty or placeholder public types.
- Existing modules and examples still compile.

## Phase 2: Core Entry Point And Parameters

Goal: make basic reference usage compile and run.

- [ ] Implement `KRandom extends java.util.Random`.
- [ ] Implement constructors:
  - [ ] `KRandom()`
  - [ ] `KRandom(KRandomParameters)`
- [ ] Implement `nextObject(Class<T>)`.
- [ ] Implement `objects(Class<T>, int)`.
- [ ] Implement `ObjectCreationException`.
- [ ] Implement `KRandomParameters` defaults and fluent methods:
  - [ ] seed `123L`
  - [ ] charset `US_ASCII`
  - [ ] string length range `1..32`
  - [ ] collection size range `1..100`
  - [ ] object pool size `10`
  - [ ] randomization depth `Integer.MAX_VALUE`
  - [ ] date range around `2020-01-01 UTC` plus/minus 10 years
  - [ ] time range `LocalTime.MIN..LocalTime.MAX`
  - [ ] `copy()`
- [ ] Bridge `KRandomParameters` into local `GeneratorConfig` and object-generation config.

Acceptance:

- Reference README sample compiles and passes.
- Default-value tests prove compatibility defaults differ from first-party `GeneratorConfig` only inside the compatibility layer.

## Phase 3: Object Generation Semantics

Goal: match the reference object-population contract.

- [ ] Preserve existing local `ObjectGenerator` behavior for first-party users.
- [ ] Add compatibility population mode for `KRandom`:
  - [ ] setter-first assignment by default
  - [ ] direct field assignment when `bypassSetters(true)`
  - [ ] preserve initialized non-default values unless `overrideDefaultInitialization(true)`
  - [ ] skip static fields
  - [ ] handle inner-class synthetic `this$0`
  - [ ] match reference exception wrapping
- [ ] Align records:
  - [ ] canonical constructor population
  - [ ] record component annotations
  - [ ] primitive defaults for excluded components
- [ ] Align cycle handling and object pool size.
- [ ] Align depth behavior with unlimited default and explicit truncation.
- [ ] Add generic hierarchy support for reference fixtures:
  - [ ] type variables in generic base classes
  - [ ] multiple type variables
  - [ ] bounded generic cases
  - [ ] documented unsupported complex cases
- [ ] Add opt-in ClassGraph concrete subtype scanning for `scanClasspathForConcreteTypes(true)`.

Acceptance:

- Imported reference tests for `KRandomTest`, records, field exclusion depth, parameters, and generic hierarchy pass or have documented accepted deviations.

## Phase 4: Public Predicate And Annotation API

Goal: support reference exclusion and declarative randomizer ergonomics.

- [ ] Implement `io.github.krandom.FieldPredicates`:
  - [ ] `named(String)` using regex matching
  - [ ] `ofType(Class<?>)`
  - [ ] `inClass(Class<?>)`
  - [ ] `isAnnotatedWith(Class<? extends Annotation>...)`
  - [ ] `hasModifiers(int)`
- [ ] Implement `io.github.krandom.TypePredicates`.
- [ ] Implement annotations:
  - [ ] `io.github.krandom.annotation.Exclude`
  - [ ] `io.github.krandom.annotation.Randomizer`
  - [ ] `io.github.krandom.annotation.RandomizerArgument`
  - [ ] `io.github.krandom.annotation.Priority`
- [ ] Support repeatable/randomizer argument conversion for primitive, wrapper, enum, string, date/time, and arrays as covered by reference tests.
- [ ] Preserve reference precedence:
  - [ ] field override
  - [ ] type override
  - [ ] annotation randomizer
  - [ ] Bean Validation
  - [ ] built-in randomizer

Acceptance:

- Imported `FieldExclusionTest`, `TypeExclusionTest`, `RandomizerAnnotationTest`, and `RandomizerProxyTest` pass.

## Phase 5: Extension SPI And Registry Model

Goal: support drop-in custom extensions written for the reference library.

- [ ] Implement `io.github.krandom.api.Randomizer<T>`.
- [ ] Implement `ContextAwareRandomizer<T>`.
- [ ] Implement `RandomizerContext`.
- [ ] Implement `RandomizerRegistry`.
- [ ] Implement `RandomizerProvider`.
- [ ] Implement `ExclusionPolicy`.
- [ ] Implement `ObjectFactory`.
- [ ] Implement `RandomizationContext` and stack item accessors needed by context-aware randomizers.
- [ ] Implement default registries:
  - [ ] custom randomizer registry
  - [ ] exclusion randomizer registry
  - [ ] annotation randomizer registry
  - [ ] internal Java type registry
  - [ ] time registry
  - [ ] Bean Validation registry when module is present
- [ ] Implement priority ordering with `@Priority`.
- [ ] Implement safe ServiceLoader discovery.

Acceptance:

- Custom registry/provider/factory/policy tests pass.
- ServiceLoader test can discover a test registry from `META-INF/services`.

## Phase 6: Built-In Randomizer Facades

Goal: expose reference randomizer classes and behavior.

- [ ] Add `io.github.krandom.randomizers.AbstractRandomizer`.
- [ ] Add collection randomizers:
  - [ ] collection, list, set, queue, map, enum set, enum map
- [ ] Add misc randomizers:
  - [ ] boolean, constant, enum, locale, null, optional, skip, UUID
- [ ] Add number randomizers:
  - [ ] byte, short, int, long, float, double, number, BigInteger, BigDecimal, atomic integer, atomic long
- [ ] Add range randomizers:
  - [ ] numeric ranges
  - [ ] legacy date/sql date ranges
  - [ ] JSR-310 date/time ranges
- [ ] Add text randomizers:
  - [ ] character
  - [ ] char sequence
  - [ ] string
  - [ ] string delegating
- [ ] Add net randomizers:
  - [ ] URI
  - [ ] URL
- [ ] Add time randomizers:
  - [ ] calendar/date/sql
  - [ ] local/offset/zoned types
  - [ ] duration/period/year/year-month/month-day
  - [ ] zone/timezone types
- [ ] Add faker randomizers:
  - [ ] first/last/full name
  - [ ] email/password/phone
  - [ ] city/state/country/street/zip
  - [ ] company
  - [ ] credit card
  - [ ] ISBN
  - [ ] IPv4/IPv6/MAC
  - [ ] latitude/longitude
  - [ ] word/sentence/paragraph/generic string
  - [ ] regular expression

Acceptance:

- Constructor signatures and public methods match the API inventory.
- Seeded same-instance determinism tests pass.
- If exact output parity is in scope, imported approval tests pass.

## Phase 7: Bean Validation Compatibility

Goal: match the reference `bean-validation` module.

- [ ] Add `io.github.krandom.validation.BeanValidationRandomizerRegistry`.
- [ ] Add annotation handlers:
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
- [ ] Support field annotations and getter/method annotations.
- [ ] Preserve custom Bean Validation registry override behavior.
- [ ] Add validator integration tests with Hibernate Validator.

Acceptance:

- Imported `bean-validation` tests pass, including "generated bean should be valid using bean validation api".

## Phase 8: Determinism And Output Compatibility

Goal: make seeded behavior explicit and testable.

- [ ] Decide exact-output parity scope for DataFaker-backed randomizers.
- [ ] If exact parity is required:
  - [ ] pin DataFaker version to the reference-compatible behavior
  - [ ] mirror locale and safe-mode constructors
  - [ ] mirror seed-to-random wiring
  - [ ] import approval files or convert them to deterministic assertions
- [ ] If exact parity is not required:
  - [ ] document deterministic-within-library compatibility only
  - [ ] assert repeatability, validity, and format constraints instead of exact strings

Acceptance:

- Determinism policy is documented in the audit and compatibility README.
- Seeded tests reflect the selected policy.

## Phase 9: Documentation, Examples, And Release Readiness

Goal: make the compatibility surface usable and maintainable.

- [ ] Add a compatibility README with installation, module mapping, and migration examples.
- [ ] Add examples for:
  - [ ] `new KRandom().nextObject(...)`
  - [ ] `KRandomParameters`
  - [ ] custom randomizer by field and type
  - [ ] custom registry
  - [ ] Bean Validation module
  - [ ] classpath subtype scanning
- [ ] Add javadocs/KDoc for public compatibility APIs.
- [ ] Add release notes describing compatibility scope.
- [ ] Run full `./scripts/pre_commit_check.sh`.

Acceptance:

- A user can follow docs without referencing the upstream repository.
- All compatibility examples compile in CI or local verification scripts.

## Risk Register

| Risk | Impact | Mitigation |
| --- | --- | --- |
| Package/API duplication creates confusion | Medium | Put compatibility types in dedicated modules and document first-party vs compatibility APIs clearly. |
| Exact DataFaker output parity is brittle | High | Prefer deterministic-validity parity unless exact approval outputs are required by product. |
| Classpath scanning adds startup cost and nondeterminism | Medium | Make it opt-in and scoped to compatibility `KRandomParameters.scanClasspathForConcreteTypes(true)`. |
| Setter-first semantics conflict with current direct-field object generation | Medium | Keep setter-first behavior inside compatibility mode only. |
| Full SPI support increases maintenance burden | High | Build adapters over existing internals where possible; keep compatibility tests close to upstream behavior. |

## Initial Task Order

1. Phase 0 API inventory and compatibility test harness.
2. Phase 1 module skeleton.
3. Phase 2 `KRandom`/`KRandomParameters` with core object generation.
4. Phase 4 predicate/annotation API, because many imported tests depend on it.
5. Phase 3 object semantics refinements.
6. Phase 5 SPI and registry model.
7. Phase 7 Bean Validation.
8. Phase 6 randomizer facades, split by package family.
9. Phase 8 determinism policy.
10. Phase 9 docs and release readiness.

## Completion Gate

The plan is complete when:

- Compatibility modules compile.
- The selected upstream test subset passes.
- Every reference public API item is implemented, intentionally excluded, or mapped to a documented replacement.
- Audit status rows in `docs/feature-parity/k-random-reference-parity.md` are updated to complete.
- `./scripts/pre_commit_check.sh` passes.
