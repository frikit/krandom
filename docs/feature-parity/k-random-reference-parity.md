# k-random/k-random Feature Parity Audit

## Reference Snapshot

- Reference repository: https://github.com/k-random/k-random
- Local clone reviewed at: `/private/tmp/k-random-reference`
- Reference commit reviewed: `43d5b6f4ea38b59ce73c90d9f47e3b25e9c57f32`
- Commit date/message: `2026-02-08 Convert krandom to kotlin (#106)`
- Upstream modules: `core`, `randomizers`, `bean-validation`
- Upstream group/artifacts: `io.github.k-random:k-random-core`, `k-random-randomizers`, `k-random-bean-validation`
- Local project reviewed from: `/Users/frikit/IdeaProjects/krandom`

## Executive Summary

100% feature parity is achievable, but the answer depends on what "100%" means.

- Functional parity for the useful workflows is mostly reachable with the current architecture. The local project already covers object generation, semantic fake data, records, Objenesis, nested graphs, collections, maps, optionals, exclusions, declarative randomizers, seedable generators, and many more domain providers than the reference.
- Strict source/API parity is not present today. The reference exposes `io.github.krandom.*` packages, `KRandom`, `KRandomParameters`, `RandomizerRegistry`, `RandomizerProvider`, `ExclusionPolicy`, `ObjectFactory`, annotation packages, module names, ServiceLoader entries, and default semantics that differ from the local `io.github.frikit.krandom.*` API.
- Exact deterministic output parity is a separate decision. Several reference tests approve exact seeded DataFaker outputs. Matching feature behavior is practical; matching every approved seeded string requires reusing the same dependencies, seeding, locale paths, and algorithms.

Recommended path: add a compatibility layer rather than reshape the main API. Keep the existing richer `GeneratorConfig`/`ObjectGenerator` model, then introduce source-compatible `io.github.krandom` facade modules that delegate into local internals and fill the missing semantics.

## Current Parity Matrix

| Area | Local Status | Strict Reference Parity | Notes |
| --- | --- | --- | --- |
| Modules and coordinates | Partial | Missing | Local has `core`, integrations, DSLs, examples, and benchmarks. Reference has `core`, `randomizers`, `bean-validation` under `io.github.k-random`. |
| Entry point | Partial | Missing | Local uses `ObjectGenerator<T>` and `Generators.ofObject`. Reference users expect `new KRandom().nextObject(MyType.class)` and `objects(type, size)`. |
| Config object | Partial | Missing | Local `GeneratorConfig` overlaps but does not expose `KRandomParameters` names/defaults/copy behavior. Reference defaults include seed `123L`, string length `1..32`, collection size `1..100`, unlimited depth. |
| POJO and record generation | High | Partial | Local supports POJOs, records, constructors, Objenesis fallback, nested graphs, inherited fields, and circular guards. Gaps: setter-first assignment, `bypassSetters`, final-field behavior, exact generic hierarchy behavior, and classpath subtype scanning. |
| Arrays, collections, maps, optionals | High | Partial | Local supports common typed containers. Reference has dedicated populator/randomizer classes and more exact concrete collection behavior. |
| Built-in Java type randomizers | High | Partial | Local covers or exceeds most scalar/date/network/identifier types. Strict parity needs reference class names under `io.github.krandom.randomizers.*` and range randomizer APIs. |
| Faker/DataFaker randomizers | High functional | Partial | Local has richer native providers. Reference exposes DataFaker-backed classes such as `FirstNameRandomizer`, `ZipCodeRandomizer`, `PasswordRandomizer`, `RegularExpressionRandomizer`, with seed/locale approval behavior. |
| Bean Validation | Partial | Partial | Local supports `@Size` on String, `@Email`, `@Pattern`, int/long min/max, decimal min/max, positive/negative. Reference adds `@AssertTrue`, `@AssertFalse`, `@Null`, `@NotBlank`, `@Past`, `@PastOrPresent`, `@Future`, `@FutureOrPresent`, collection/map/array `@Size`, method/getter annotations, and registry/service loading. |
| Extension SPI | Low | Missing | Reference has `Randomizer<T>`, `ContextAwareRandomizer<T>`, `RandomizerContext`, `RandomizerRegistry`, `RandomizerProvider`, `ExclusionPolicy`, `ObjectFactory`, `@Priority`, and ServiceLoader discovery. Local has generator/contextual APIs but not the same SPI. |
| Annotations | Medium | Partial | Local has analogous `@Exclude`, `@Randomizer`, `@RandomizerArgument`, plus `@Fake` and `@FakeRange`. Strict package names and constructor-argument conversion behavior need alignment. |
| Classpath scanning | Missing | Missing | Reference can opt into ClassGraph-based concrete subtype discovery for abstract/interface fields. Local intentionally avoids this today. |
| Setter semantics | Missing | Missing | Reference calls setters by default and only direct-fields with `bypassSetters(true)`. Local object generation sets fields directly. |
| Kotlin/source compatibility | Partial | Missing | Reference implementation is Kotlin but exposes Java-friendly APIs in `io.github.krandom`. Local has a Kotlin DSL but not the same Kotlin package/API surface. |
| Docs/examples | Strong local | Partial | Local docs are broader. Strict parity needs examples that compile against the compatibility API. |

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
- More integrations: Jackson, Spring Boot starter, jqwik, Kotest, Kotlin DSL, examples, docs site, benchmarks.
- Richer object faker authoring API with named profiles, include/ignore paths, post-processing, semantic modes, unique field tracking, and provider-backed semantic fields.
- Stronger release/test infrastructure in this repository, including coverage gates and docs verification scripts.

## Main Gaps To Close

1. Source-compatible facade packages:
   - Add `io.github.krandom.KRandom`, `KRandomParameters`, `FieldPredicates`, `TypePredicates`, `ObjectCreationException`, annotations, and `api` interfaces.
   - Preserve upstream names, method signatures, default constants, and Java/Kotlin interop behavior.

2. Compatibility module layout:
   - Add modules that publish or at least build as `k-random-core`, `k-random-randomizers`, and `k-random-bean-validation` equivalents under local control.
   - Include `META-INF/services/io.github.krandom.api.RandomizerRegistry` entries.

3. Object-generation semantic differences:
   - Implement setter-first assignment with `bypassSetters(false)` default.
   - Add `bypassSetters(true)` direct-field mode.
   - Revisit final-field behavior if strict parity requires attempting reflective population.
   - Add optional ClassGraph scanning for concrete subtypes.
   - Add reference-compatible generic superclass resolution tests.
   - Align default seed, default depth, default string/collection sizes, date/time reference ranges, and exception wrapping.

4. Extension SPI:
   - Implement adapters for `Randomizer`, `ContextAwareRandomizer`, `RandomizerContext`, `RandomizerRegistry`, `RandomizerProvider`, `ExclusionPolicy`, and `ObjectFactory`.
   - Preserve reference priority ordering: custom/exclusion/user/service registries, then annotation, bean validation, time, internal registries.

5. Bean Validation:
   - Add missing constraints: assert true/false, null, not blank, past/future variants, collection/map/array size, and method/getter annotation lookup.
   - Decide whether this remains inline in `core` or becomes a dedicated compatibility module.

6. Reference randomizer class facades:
   - Provide `io.github.krandom.randomizers.*` class names and constructor overloads.
   - Where local generators are equivalent, delegate.
   - Where deterministic approval output matters, use the same DataFaker dependency and seeding path as the reference.

## Proposed Implementation Plan

### Phase 0: Define parity contract

- Decide whether "100%" includes source compatibility, behavioral compatibility, exact seeded outputs, and Maven coordinate compatibility.
- Import a focused subset of upstream tests as compatibility tests before implementation.

### Phase 1: Core facade

- Add a compatibility source set or module with `io.github.krandom` packages.
- Implement `KRandom` and `KRandomParameters` on top of existing `ObjectGenerator`/`GeneratorConfig`.
- Add tests for `nextObject`, `objects`, defaults, seed behavior, config copying, errors, records, arrays, collections, maps, optionals, and cycles.

### Phase 2: Object semantics

- Add setter-first assignment mode and `bypassSetters`.
- Add classpath scanning behind `scanClasspathForConcreteTypes(true)`.
- Align depth, pool, default initialization, generic hierarchy, final field, static field, and inner-class exclusion behavior.

### Phase 3: SPI and registry model

- Implement reference SPI interfaces and adapters.
- Add ServiceLoader discovery and priority ordering.
- Test custom field/type randomizers, context-aware randomizers, custom providers, custom factories, custom exclusion policy, and registry precedence.

### Phase 4: Bean Validation compatibility

- Build the missing constraint handlers.
- Cover both field annotations and method/getter annotations.
- Add collection/map/array `@Size` support and full validator integration tests.

### Phase 5: Randomizer facade modules

- Add class-compatible wrappers for primitive, number, range, collection, text, net, misc, time, and faker randomizers.
- Decide whether exact DataFaker output parity is required for seeded approval tests.

### Phase 6: Documentation and release surface

- Add migration/compatibility docs.
- Add examples that compile against the compatibility API.
- Document any intentional deviations if exact drop-in parity is not selected.

## Effort Estimate

For behavioral/source parity without exact seeded DataFaker approval outputs: roughly 3-5 focused engineering weeks.

For strict drop-in parity including package names, module names, ServiceLoader behavior, all upstream tests, and deterministic seeded approvals: closer to 5-8 weeks, mostly because the compatibility SPI, Bean Validation module, and exact reference defaults need careful test-driven alignment.

## Recommendation

Do not rewrite the main library around the reference API. The local project is already broader and more cohesive as a first-party generator toolkit. Implement source-compatible facades and semantic adapters only if drop-in compatibility is a product goal. If the goal is "feature parity" rather than "API replacement", close the Bean Validation and object-semantics gaps first, then document registry/classpath scanning as optional compatibility features.
