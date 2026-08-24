# k-random/k-random Feature Parity Audit

## Reference Snapshot

- Reference repository: https://github.com/k-random/k-random
- Local clone reviewed at: `/private/tmp/k-random-reference`
- Reference commit reviewed: `43d5b6f4ea38b59ce73c90d9f47e3b25e9c57f32`
- Commit date/message: `2026-02-08 Convert krandom to kotlin (#106)`
- Upstream modules: `core`, `randomizers`, `bean-validation`
- Upstream group/artifacts: `io.github.k-random:k-random-core`, `k-random-randomizers`, `k-random-bean-validation`
- Local project reviewed from: `/Users/frikit/IdeaProjects/krandom`
- Native inventory and mapping baseline: `docs/feature-parity/k-random-reference-feature-inventory.md`

## Executive Summary

100% feature parity is achievable, but the answer depends on what "100%" means.

- Functional parity for the useful workflows is mostly reachable with the current architecture. The local project already covers object generation, semantic fake data, records, Objenesis, nested graphs, collections, maps, optionals, exclusions, declarative randomizers, seedable generators, and many more domain providers than the reference.
- Source/API parity is intentionally not the target. The reference exposes `io.github.krandom.*` packages, `KRandom`, `KRandomParameters`, `RandomizerRegistry`, `RandomizerProvider`, `ExclusionPolicy`, `ObjectFactory`, annotation packages, module names, ServiceLoader entries, and default semantics that differ from the local `io.github.frikit.krandom.*` API. These should be mapped in migration docs or represented by native krandom features.
- Exact deterministic output parity is a separate decision. Several reference tests approve exact seeded DataFaker outputs. Matching feature behavior is practical; matching every approved seeded string requires reusing the same dependencies, seeding, locale paths, and algorithms.

Recommended path: implement missing behavior natively in the existing `io.github.frikit.krandom.*` API and provide a migration guide from k-random APIs to krandom APIs. Do not add `io.github.krandom` facade packages or `k-random-*` compatibility modules unless the product goal changes to drop-in source compatibility.

## Current Parity Matrix

| Area | Local Status | Native Feature Parity | Notes |
| --- | --- | --- | --- |
| Modules and coordinates | N/A | Migration mapping needed | Local has `core`, integrations, DSLs, examples, and benchmarks. Reference has `core`, `randomizers`, `bean-validation`; do not recreate those modules. |
| Entry point | High | Covered through native APIs | Local uses `ObjectGenerator<T>` and `Generators.ofObject`. Migration docs map `new KRandom().nextObject(...)` and `objects(type, size)` to local generation APIs. |
| Config object | High | Covered for mapped settings | `GeneratorConfig` covers the main `KRandomParameters` behaviors through native names. Migration docs note default differences and replacement decisions. |
| POJO and record generation | High | Partial | Local supports POJOs, records, constructors, Objenesis fallback, nested graphs, inherited fields, and circular guards. Gaps: setter-first assignment, `bypassSetters`, final-field behavior, exact generic hierarchy behavior, and classpath subtype scanning. |
| Arrays, collections, maps, optionals | High | Partial | Local supports common typed containers. Reference has dedicated populator/randomizer classes and more exact concrete collection behavior. |
| Built-in Java type randomizers | High | Covered | Scalar, number, atomic, Java time, legacy date/time, URI/URL, locale, UUID, text/regex, collection/object-field, and faker/domain randomizer families now have native generators or documented generator-composition replacements. |
| Faker/DataFaker randomizers | High functional | Partial | Local has richer native providers. Reference exposes DataFaker-backed classes such as `FirstNameRandomizer`, `ZipCodeRandomizer`, `PasswordRandomizer`, `RegularExpressionRandomizer`; map those to native generators. |
| Bean Validation | High | Covered through native APIs | Local object generation now supports the reference constraint families for fields and getter/method annotations, including assert true/false, null/not-blank, numeric bounds/signs, decimal bounds, past/future temporal constraints, pattern/email, and string/container/map/array `@Size`. Reference registry/service loading maps to native resolver behavior rather than a separate module. |
| Extension SPI | Medium | Covered through native APIs and migration docs | Reference has `Randomizer<T>`, `ContextAwareRandomizer<T>`, registries/providers/policies/factories. Local maps these to `Generator<T>`, rich `ContextualGenerator<T>` metadata, predicate/object overrides, and explicit configuration-scoped `KRandomModule` contributions. |
| Annotations | High | Covered for reference-style field rules | Local has analogous `@Exclude`, `@Randomizer`, `@RandomizerArgument`, plus `@Fake` and `@FakeRange`. Constructor arguments cover common reference-style value types. |
| Classpath scanning | Missing by design | Migration-doc-only replacement | Reference can opt into ClassGraph-based concrete subtype discovery for abstract/interface fields. Local migration uses explicit `objectOverride(...)` registrations instead of scanning. |
| Setter semantics | Direct-field native behavior | Migration-doc-only replacement | Reference calls setters by default and only direct-fields with `bypassSetters(true)`. Local object generation sets fields directly; setter-first mode is not copied in the native parity plan. |
| Kotlin/source compatibility | Partial | Migration mapping needed | Reference implementation is Kotlin but exposes Java-friendly APIs in `io.github.krandom`. Local has a Kotlin DSL under this project's package surface. |
| Docs/examples | Strong local | Covered | Migration guide, docs-site entry, docs index link, README link, and native compile-backed examples are in place. |

## Recheck Conclusion

Rechecked against upstream `k-random/k-random` `v2.2.20` (`43d5b6f4ea38b59ce73c90d9f47e3b25e9c57f32`):

- Native migration parity is complete under the agreed contract: every reference feature is implemented natively, mapped to a native replacement, or documented as an intentional non-goal.
- Drop-in source/API parity is not complete and remains out of scope: no `io.github.krandom.*` packages, no `k-random-*` compatibility artifacts, no `KRandom` facade, no reference `RandomizerRegistry`/ServiceLoader surface, and no exact seeded output cloning.
- The migration guide now names every reference public randomizer class, including range randomizers and time component randomizers.
- Remaining differences are product decisions, not untracked implementation phases: setter-first population, classpath subtype scanning, full `RandomizerContext`, `ObjectFactory` hook, final-field population attempts, registry priority/ServiceLoader discovery, and exact upstream DataFaker strings.

## Phase 0 Mapping Baseline

The detailed inventory is now captured in `docs/feature-parity/k-random-reference-feature-inventory.md`. The important Phase 0 conclusions are:

- Entry-point parity is covered natively by `Generators.ofObject(Type.class)`, `ObjectGenerator<T>`, and `generateList(size)`.
- Configuration parity is mostly covered by `GeneratorConfig`; the remaining native deviations are setter-first mode, classpath concrete subtype scanning, reference registry ServiceLoader discovery, and a root-level object time range.
- Object graph generation already covers the major reference behaviors: records, classes, nested objects, inherited fields, arrays, collections, maps, optionals, object pool behavior, max depth, circular references, Objenesis fallback, and seed propagation.
- Predicate and annotation parity is partial: local field predicates cover common cases, but reference regex name matching, varargs annotation matching, and most `TypePredicates` helpers need native additions or documented custom predicates.
- Built-in randomizer capability parity is broad. Most reference randomizers map to scalar factories, object field support, or domain namespaces. The main public-facade gaps are standalone atomics, some standalone time types, and collection randomizer classes.
- Bean Validation parity is now implemented natively in `krandom-core`. The remaining difference is packaging: krandom does not add a `k-random-bean-validation` module or registry ServiceLoader surface.

## Phase 2 Configuration Baseline

Native configuration mapping is now covered by tests in `ObjectGeneratorConfigurationMappingTest`.

- `seed(long)`, `charset(...)`, `stringLengthRange(...)`, `collectionSizeRange(...)`, `objectPoolSize(...)`, `randomizationDepth(...)`, `dateRange(...)`, `ignoreRandomizationErrors(...)`, and `overrideDefaultInitialization(...)` all have tested `GeneratorConfig` equivalents.
- `timeRange(...)` migrates to direct `TimeGenerator` usage or an explicit `LocalTime` object override.
- `bypassSetters(true)` maps to krandom's existing direct-field object population; no setter-first mode is added.
- `scanClasspathForConcreteTypes(true)` maps to explicit type or field overrides for abstract/interface fields; no ClassGraph-style scanning is added.
- Defaults differ intentionally: k-random has seed `123`, string size `1..32`, collection size `1..100`, and effectively unlimited depth; krandom keeps its native defaults unless users configure them explicitly.

## Phase 3 Exclusion And Declarative Rule Baseline

Native exclusion and annotation mapping is now covered by tests in `ObjectGeneratorRuleMappingTest`.

- `FieldPredicates` covers exact names, regex name matching, field type, declaring class, varargs annotation matching, modifiers, and normal Java predicate composition.
- `TypePredicates` covers exact names, exact type, package prefixes, varargs annotation matching, interface, abstract, modifiers, enum, array, and assignability checks.
- Programmatic exclusions apply through nested object graphs and inherited fields.
- `@Exclude` maps to `io.github.frikit.krandom.generator.object.Exclude`.
- `@Randomizer` maps to native `Generator<?>` implementations.
- `@RandomizerArgument` now converts common reference-style constructor argument types, including arrays and date/time values.

## Phase 4 Extension Model Baseline

Native extension mapping is now covered by tests in `ObjectGeneratorExtensionModelTest`.

- k-random `Randomizer<T>` maps to native `Generator<T>`.
- k-random `ContextAwareRandomizer<T>` maps to native `ContextualGenerator<T>`, with context for field name, owner type, depth, full path, declared type, declaration, and active config.
- Type, exact field, predicate field, and contextual predicate field randomizer registration are covered through `GeneratorConfig.objectOverride(...)`.
- Reusable registry/provider use cases map to explicit, config-scoped `KRandomModule` contributions; short-lived hub-local extensions can still use `ProviderHub.register(...)`.
- Object factory customization maps to native constructor/Objenesis creation plus explicit type or field overrides for special cases.
- Ambient ServiceLoader registry discovery and registry priority annotations remain intentionally out of scope; modules are explicitly installed, deterministically ordered, and conflict-safe.

## Phase 5 Built-In Randomizer Baseline

Native built-in randomizer capability parity is now covered by tests in `BuiltInGeneratorCatalogTest`.

- Primitive/wrapper, big-number, `Number`, `AtomicInteger`, and `AtomicLong` reference randomizers map to native scalar factories and standalone generator classes.
- Java time and legacy date/time randomizers map to native facade methods, `Generators.datetime()`, `Generators.forType(...)`, and object field resolution. `LegacyTimeZoneGenerator` covers `java.util.TimeZone`; `TimezoneGenerator` remains the string timezone-id generator.
- Reference `UriRandomizer` and `UrlRandomizer` map to typed `Generators.ofURI()` and `Generators.ofURL()` when Java objects are needed, or existing string `ofUri()` and `ofUrl()` generators for URL-form strings.
- Text and regex randomizers map to `CharGenerator`, `StringGenerator`, text namespace generators, and `Generators.ofRegex(...)`.
- Collection, map, enum collection, optional, null, and skip randomizers map to object field population, `generateList`, `repeat`, selection helpers, constants, and exclusions.
- Faker/DataFaker randomizers map to native person, location, finance, network, identifier, text, and base generators.

## Phase 7 Determinism Baseline

Native determinism and migration guarantees are now covered by tests in `ObjectGeneratorDeterminismTest`.

- Migrated `nextObject(Class)` examples are repeatable when the same krandom seed and config are used.
- Migrated `objects(Class, size)` examples are repeatable when the same krandom seed and config are used.
- Field and type override examples remain repeatable when the override generators are deterministic.
- Faker/domain generator examples are repeatable under the same krandom seed, locale, version, and call order.
- Exact k-random/DataFaker output strings remain out of scope. Migration preserves explicit seed intent, not upstream approved-output snapshots.

## Phase 8 Migration Documentation Baseline

Native migration documentation is now covered by `docs/migration/k-random-to-krandom.md`, the docs-site migration page, and tests in `ObjectGenerationDocumentationExamplesTest`.

- Install coordinates map `io.github.k-random:k-random-*` artifacts to `io.github.frikit:krandom-core`.
- `KRandom`, `KRandomParameters`, randomizer classes, annotations, extension points, and Bean Validation have explicit mapping tables.
- Required before/after examples cover basic object generation, seeded generation, field overrides, type overrides, exclusions, Bean Validation, and faker/domain generators.
- README, docs index, and the public docs-site guides index all link to migration material.

## Reference Feature Surface

The reference codebase is an Easy Random fork, not primarily a fake-data catalog. Its core surface is:

- `KRandom extends java.util.Random` with `nextObject(Class<T>)` and `objects(Class<T>, int)`.
- `KRandomParameters` fluent settings: seed, charset, string length range, collection size range, object pool size, randomization depth, date/time ranges, scan classpath flag, ignore errors, override default initialization, bypass setters, custom object factory, custom provider, custom registries, field/type randomizers, field/type exclusions.
- Recursive object population with arrays, collections, maps, optionals, records, generic superclass resolution, object pool cycle handling, and Objenesis instantiation.
- Public SPI: randomizers, context-aware randomizers, randomizer registries/providers, object factories, exclusion policies, priority ordering, and ServiceLoader registry discovery.
- Built-in randomizer families: primitive/wrapper, number, range, text, collection, misc, net, time, and faker/DataFaker adapters.
- Bean Validation module with constraint-aware randomizers loaded as a registry.

## Local Strengths Versus Reference

- Much broader first-party fake-data catalog: names, locations, finance, identifiers, commerce, network, text, files, schema output, provider hub, and semantic object generation.
- More integrations: Jackson, Spring Boot starter, Kotest, Kotlin DSL, examples, docs site, benchmarks.
- Richer object faker authoring API with named profiles, include/ignore paths, post-processing, semantic modes, unique field tracking, and provider-backed semantic fields.
- Stronger release/test infrastructure in this repository, including coverage gates and docs verification scripts.

## Main Gaps To Close

1. Native migration mapping:
   - Map each reference feature to an existing krandom API, a missing native feature, a migration-doc-only replacement, or an intentional non-goal.
   - Keep package names and artifact coordinates under this project's existing API surface.

2. Object-generation semantic differences:
   - Implement setter-first assignment with `bypassSetters(false)` default.
   - Add `bypassSetters(true)` direct-field mode.
   - Revisit final-field behavior if strict parity requires attempting reflective population.
   - Add optional ClassGraph scanning for concrete subtypes.
   - Add reference-compatible generic superclass resolution tests.
   - Align default seed, default depth, default string/collection sizes, date/time reference ranges, and exception wrapping.

3. Native extension equivalents:
   - Covered by `Generator`, `ContextualGenerator`, field/type/predicate overrides, `ProviderHub`, and explicit construction decisions.

4. Bean Validation:
   - Covered natively in object generation for reference constraint families.
   - Keep behavior native to existing krandom modules; no first-party compatibility module is justified.

5. Reference randomizer capability coverage:
   - Covered by Phase 5 native generators, facade methods, `forType(...)` lookup, object field resolution, and migration-table replacements.
   - Prefer existing local generators and provider namespaces over class-name facades for future randomizer-family additions.

## Proposed Implementation Plan

### Phase 0: Define native parity contract

- Confirm that "100%" means native feature parity, not source/import compatibility.
- Build a reference feature inventory and map each item to local APIs or missing native work.
- Create a migration guide shell.

### Phase 1: Native object generation parity

- Cover `KRandom.nextObject(...)` and `objects(...)` workflows with local `Generators.ofObject(...)`, `ObjectGenerator`, and list/stream generation examples.
- Add native tests for records, arrays, collections, maps, optionals, cycles, depth, and object pool behavior.

### Phase 2: Native configuration mapping

- Map `KRandomParameters` settings to `GeneratorConfig` settings and document default differences.
- Decide whether setter-first and classpath-scanning behavior should be added natively or documented as explicit override/provider replacements.

### Phase 3: Exclusions and declarative rules

- Verify or add local tests for field/type exclusions, local annotations, constructor arguments, precedence, and inherited/nested cases.
- Document migration examples.

### Phase 4: Native extension equivalents

- Completed: `Randomizer` -> `Generator`, `ContextAwareRandomizer` -> `ContextualGenerator`, predicate field randomizers, registry/provider replacements, and factory/policy decisions are documented and tested.

### Phase 5: Native randomizer capability parity

- Map every reference randomizer class to an existing local generator or a new native generator.
- Document exact-output non-goals and deterministic local behavior.

### Phase 6: Bean Validation feature parity

- Completed. Native constraint handlers cover assert true/false, null/not-blank, numeric and decimal bounds, sign constraints, past/future temporal constraints, pattern/email, and `@Size` for strings, arrays, collections, queues, sets, lists, and maps.
- Field annotations, record/accessor annotations, JavaBean getters, boolean getters, and interface accessor declarations are covered.
- Hibernate Validator integration tests prove generated objects satisfy supported constraints.

### Phase 7: Determinism and compatibility guarantees

- Completed: document the krandom-local seeded repeatability contract.
- Completed: document exact k-random/DataFaker output strings as a non-goal.
- Completed: add migrated-example repeatability tests.

### Phase 8: Migration documentation

- Completed: `docs/migration/k-random-to-krandom.md` now covers native migration from k-random without source-compatible imports.
- Completed: before/after examples cover object generation, configuration, exclusions, custom generators, Bean Validation, and faker/domain generators.
- Completed: compile-backed examples prove the migrated krandom snippets stay runnable.

## Effort Estimate

For native feature parity plus migration docs: roughly 3-5 focused engineering weeks.

For strict drop-in parity including package names, module names, ServiceLoader behavior, all upstream tests, and deterministic seeded approvals: out of scope under the current direction.

## Recommendation

Do not rewrite the main library around the reference API. The local project is already broader and more cohesive as a first-party generator toolkit. Close native behavior gaps first, then document migration mappings from k-random to krandom.
