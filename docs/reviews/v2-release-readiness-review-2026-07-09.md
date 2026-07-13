# kRandom v2 Release-Readiness Review

**Date:** 2026-07-09
**Scope:** Entire repository at `e008cfb` (`main`, version `1.6.0-SNAPSHOT`)
**Decision:** **No-go for v2 GA or organization-wide rollout today**
**Implementation plan:** [`../development/v2-master-implementation-plan.md`](../development/v2-master-implementation-plan.md)

## Executive verdict

kRandom already has the difficult part of a useful fixture platform: a coherent Java-first generator API, strong locale coverage, seeded generation, semantic object fixtures, schema exports, multiple framework adapters, extensive tests, and unusually strict local quality gates. It is not a toy library.

The current risk is not missing generator families. It is that some public promises are wider than the behavior underneath them. The most serious examples are silent invalid object creation, ambiguous random-source ownership, integrations that do not preserve their host framework's reproducibility model, and release/documentation machinery that cannot yet protect a stable v2 contract.

The right v2 is therefore a **contract release**, not a feature-count release. Freeze new provider work, harden the foundation, reduce and baseline the public API, publish a bridge release with deprecations, then run v2 release candidates through real consumer projects before GA.

Do not deploy the current snapshot across every project. Start organization-wide adoption only after the P0 gates in this review pass and at least two representative projects have completed an RC pilot with an easy rollback path.

## Product and ideology

kRandom's strongest product idea is not “another faker.” It is one source of reproducible, realistic-enough fixture data across four levels:

1. Small composable `Generator<T>` values.
2. Locale-aware domain providers.
3. Reflection-driven object graphs with semantic field inference.
4. Schemas and framework adapters for tests, exports, and application fixtures.

The implementation consistently points to these principles:

- Java is the source of truth; Kotlin and test-framework modules should be thin adapters.
- One configuration should control seed, locale, clock, nullability, size, depth, and provider behavior.
- A seed should make failures reproducible.
- Generated objects should look semantically coherent, not merely type-correct.
- Extensions should be explicit through generators, providers, registries, and field rules.
- Integrations should preserve the conventions of JUnit, jqwik, Kotest, Jackson, and Spring.
- Correctness and clarity matter more than clever abstractions.
- Competitor parity informs the roadmap but should not force source-compatible aliases into the permanent API.

That is a sound ideology. v2 should make it enforceable.

## What is already strong

- The module boundary is understandable: core, Jackson, JUnit, Spring Boot, Kotest, Kotlin DSL, benchmarks, and executable consumer examples.
- Core has roughly 46,000 production lines and 52,000 test lines, with about 3,300 tests and an enforced 99.9% JaCoCo gate across counters.
- Locale/provider resources are substantial and packaged reproducibly.
- The schema export code streams JSONL, CSV, XML, and SQL rather than materializing every record.
- Recent work fixed several risks recorded in older reviews: scoped registries for the original provider families, locale fallback, collection subtype handling, registry input validation, and opt-in `mavenLocal()`.
- CI exercises Java, Kotlin, Scala, Maven, Gradle, and Mill consumers.
- Release artifacts are signed and include sources and Javadoc.
- Existing tests show serious attention to escaping, determinism, null handling, recursion, locale fallback, validation shapes, and boundary behavior.

These strengths justify hardening the current design instead of replacing it.

## Verification performed

- `JAVA_HOME=<Temurin 21> ./scripts/pre_commit_check.sh` passed formatting, Markdown, docs-site links, compilation, Javadoc, all tests, and the 99.9% all-counter coverage gate. Reported totals were 13,422/13,427 lines and 4,852/4,856 branches covered.
- `JAVA_HOME=<Temurin 21> ./scripts/verify_examples_local.sh` passed locally published artifact tests for Java/Gradle, Java/Maven, integration modules, Kotlin/Gradle, Kotlin/Maven, Scala/sbt, and Scala/Mill.
- Maven Central metadata and GitHub releases both identify `1.5.0` as the latest GA release. The repository is developing `1.6.0-SNAPSHOT`.
- Focused executable probes reproduced the object/constraint/random findings: null Kotlin `val` properties, null nested-generic elements, violated `@NotNull`, unsatisfiable constraints producing invalid values, misleading secure/caller-random combinations, and lost reseeding after `map`.
- `./gradlew help --warning-mode all` succeeds but reports a Gradle 10 incompatibility inside `com.gradleup.nmcp.settings` 1.6.1: it passes a `Project` object as dependency notation. This must be resolved by upgrading/fixing the publication plugin before Gradle 10 adoption.
- GitHub currently has no open issues, so the accepted items from this review need to be converted into a visible milestone/backlog before implementation begins.

Passing the existing gate is valuable evidence of implementation discipline, but it does not invalidate the targeted failures: the suite does not currently encode those public-contract scenarios.

## Release blockers — P0

### P0.1 Make object generation valid or fail loudly

**Evidence**

- `ObjectGenerator` falls back to Objenesis and `FieldGeneratorResolver` only assigns writable fields. An immutable Kotlin data class with `val` constructor properties is returned with all supposedly non-null properties set to `null`.
- The Kotlin DSL and Kotest documentation suggest data-class support, while their tests use mutable no-argument classes.
- Nested generic elements are reduced to `Object.class`; a field such as `List<List<String>>` produces null elements.
- `BeanValidationSupport` does not model `@NotNull` or `@NotEmpty`. A null probability of `1.0` can therefore violate `@NotNull`.
- Contradictory constraints are collapsed silently: `@Min(10) @Max(5)` can emit `10`, and `@Size(min=5,max=2)` can emit length five.
- Collection/map/array helpers catch insertion and assignment failures and return partial fixtures even when the caller did not request ignored errors.
- Constructor bypass can violate invariants even when fields happen to be populated.

**Required change**

1. Introduce an explicit construction policy: safe constructors/records first, unsafe constructor bypass opt-in.
2. Add a recursive `Type` representation for parameterized types, wildcards, arrays, and type variables, or reject unsupported shapes with the complete field path.
3. Normalize Bean Validation constraints into one checked model; reject unsatisfiable combinations before generating.
4. Support `@NotNull` and `@NotEmpty`, and validate generated fixtures in integration tests with an actual Jakarta Validation provider.
5. Stop swallowing assignment/insertion errors unless an explicit `ignoreErrors` mode is active. Include owner type, field path, declared type, and attempted value category in the exception.
6. Either implement Kotlin primary-constructor generation in the Kotlin module or reject immutable Kotlin classes clearly. Silent invalid instances are not acceptable.

**Acceptance criteria**

- Immutable Kotlin data classes are correctly constructed or rejected before returning a value.
- Nested generic fixture tests cover lists, maps, wildcards, records, and Kotlin types.
- Every advertised Bean Validation constraint has positive, nullability, boundary, and contradiction tests against Hibernate Validator.
- Default object generation never returns a fixture after a suppressed assignment failure.

### P0.2 Define one deterministic random-source contract

**Evidence**

- `GeneratorConfig` is described as immutable but can retain a mutable caller-owned `Random` and mutable extension objects.
- Random-source precedence is surprising: `secureRandom().seed(7)` reports secure mode but creates `Random`, and a caller-supplied `Random` can win while the config still reports a numeric seed.
- Top-level object generation does not follow the same random factory/seed path as scalar generators.
- `map` and `filter` wrap a `Seedable` generator in lambdas that lose reseeding capability.
- Several Javadocs overstate independence and incorrectly describe `java.util.Random` sharing as corrupting state. The real problems are contention, interleaving, and lost replayability.
- Bounded generators accept reversed bounds by swapping them although the public contract says `min >= max` is invalid.

**Required change**

1. Specify random-source precedence and ownership for seed, caller random, secure mode, and random factory.
2. Prefer per-generator `RandomGenerator` creation from a factory or splittable source; do not expose ambiguous shared mutable state as immutable configuration.
3. Derive named child streams for object fields and schema columns so unrelated additions do not reorder every downstream value.
4. Make combinators preserve `Seedable`, or replace optional runtime reseeding with a type-safe contract.
5. Choose strict or normalized bound semantics and use it everywhere.
6. Add a serializable generation recipe containing library version, algorithm/version, seed, locale, clock, profile, and provider dataset version.

**Acceptance criteria**

- A truth table documents and tests every random-source combination.
- Scalar, object, schema, JUnit, jqwik, Kotest, and Kotlin APIs replay the same recipe deterministically.
- Composed generators preserve supported seed behavior.
- Golden streams protect the patch-level reproducibility promise in `VERSIONING.md`.

### P0.3 Use the v2 boundary to simplify and baseline the public API

**Evidence**

- `Generators` is 2,881 lines with 337 public static methods and 199 distinct method names.
- It contains overlapping aliases such as `constant`/`ofConstant`, `pick`/`pickFrom`, `unique`/`uniqueValues`, URL/URI casing pairs, and inconsistent overload sets.
- `ObjectGeneratorConfig` duplicates much of `GeneratorConfig` in 926 package-private lines even though the public story says there is one configuration path.
- The versioning policy treats exported documented APIs as stable, but no Revapi or japicmp baseline blocks accidental binary/source breaks.
- There are currently no deprecations, so a direct v2 removal would give consumers no bridge.

**Required change**

1. Define a canonical public API inventory and classify each member: keep, rename, deprecate, internalize, or remove in v2.
2. Prefer small domain namespaces/builders over adding more methods to the facade.
3. Collapse object configuration onto `GeneratorConfig` or a small composed `ObjectGenerationConfig` without mirrored state.
4. Add binary/source compatibility verification against the latest GA artifact.
5. Release a final 1.6.x bridge with deprecations and migration recipes before removing aliases in v2.

**Acceptance criteria**

- A checked-in API surface and compatibility baseline exist.
- Every v2 removal has a 1.6 deprecation and documented replacement unless it fixes unsafe behavior that cannot remain.
- Public Javadocs no longer reference package-private configuration types.
- No new facade alias is accepted without a demonstrated consumer need.

### P0.4 Make each integration obey its host framework

**Evidence**

- Kotest `toArb()` uses `arbitrary { generate() }`, ignoring Kotest's `RandomSource`. Kotest seed replay therefore cannot control an unseeded mutable kRandom generator. The adapter supplies no shrinker or edge cases.
- Kotlin DSL rules use string field names, so typos are discovered only at runtime or may remain unused. Its default `objectOverrideDefaultInitialization(true)` differs from Java defaults without a documented reason.
- `@KrandomTest` claims to be a standalone Spring test slice, but it does not compose Spring's test extension; its own test adds `@ExtendWith(SpringExtension.class)` separately.
- Spring properties say the unseeded default is `SecureRandom`, while core defaults to `Random`.

**Required change**

1. Adapt Kotest's `RandomSource` into deterministic per-case generation and add a replay test. Document and, where meaningful, implement shrinking and edge cases.
2. Add Kotlin `KProperty1`-based field rules, validate rules at build time, align defaults, and resolve immutable-class support.
3. Make `@KrandomTest` a real composed annotation and prove it with a consumer test that uses no additional annotations.
4. Generate integration documentation from tested examples and shared configuration facts.

**Acceptance criteria**

- Re-running a failing Kotest seed reproduces the same fixture sequence.
- Kotlin rules are refactor-safe and immutable classes have explicit behavior.
- A test with only `@KrandomTest` starts the promised context.
- Adapter defaults do not contradict core defaults.

### P0.5 Make the release and documentation process trustworthy

**Evidence**

- Many public guides still use `1.0.0`; the working tree is `1.6.0-SNAPSHOT` and the README advertises `1.5.0`.
- Locale documentation alternates between 20, 35, and 50 without distinguishing native datasets from curated fallbacks.
- Locale contribution paths predate the resource reorganization.
- Some national-ID guides say checksum-valid while several implementations only produce a syntactically valid shape.
- The release workflow publishes to Central before creating the GitHub tag/release, making partial success and duplicate reruns difficult to recover from.
- The current Central publication plugin emits a deprecation that becomes an error in Gradle 10.
- The Gradle wrapper lacks `distributionSha256Sum`; dependency verification/locking, SBOM, and build provenance are absent.
- Most GitHub Actions use floating major tags, and the Mill launcher is downloaded and executed without checksum verification.
- The README says monthly benchmarks, but there is no current automated monthly workflow.

**Required change**

1. Use one version source for snippets, test or compile documentation examples, and add facts tests for locale/support counts.
2. Define data-validity language: format-valid, checksum-valid, semantically plausible, and non-routable/test-safe.
3. Move to a release-PR plus immutable tag-triggered publication flow with explicit Central confirmation and post-publication smoke tests.
4. Resolve the Central plugin's Gradle 10 incompatibility, verify the wrapper distribution, pin CI actions and downloaded tools, enable dependency verification, generate an SBOM and provenance, and document credential recovery.
5. Either automate benchmark cadence or remove the claim.

**Acceptance criteria**

- No hard-coded stale release versions remain in current guides.
- Release dry-runs check version, changelog, tag, docs, artifacts, signatures, compatibility, and consumer examples.
- A failed release can be resumed without republishing an immutable version.
- Every downloaded executable and CI action is pinned and verified.

### P0.6 Make generated financial and identity data safe by default

**Evidence**

- Credit cards are random Luhn-valid numbers built from real network IIN ranges. The documentation says they are not associated with real accounts, which the generator cannot guarantee.
- National IDs, IBANs, and similar values vary between shape-valid and checksum-valid, but their API and documentation do not expose that distinction consistently.

**Required change**

1. Default to official processor test ranges/numbers or deliberately non-routable synthetic values where a safe convention exists.
2. Make validation-valid production-like values an explicit opt-in mode.
3. Expose validity/safety metadata in provider documentation and schema metadata.

**Acceptance criteria**

- Defaults cannot accidentally resemble routable payment credentials where a safe test range is available.
- Every identity/finance provider declares and tests its validity tier.

## High priority before v2 GA — P1

### P1.1 Unify provider catalogs and registry isolation

`DataRegistryContext` scopes only the original provider families. Newer registries such as zodiac, pronoun, hobby, nationality, measurement, financial term, restaurant type, and weather remain global. `ProviderHub`, `FieldLookup`, and `SemanticFieldRegistry` also maintain overlapping hard-coded catalogs and aliases.

Create one typed `ProviderCatalog` containing canonical keys, aliases, output type, factory, metadata, locale support, validation, and fallback policy. Make all registries context-scoped. Keep global registration only as an explicitly deprecated legacy bridge. Return immutable snapshots rather than live unmodifiable concurrent-map views.

### P1.2 Refactor the object subsystem only after characterization

The resolver, semantic adjuster, and object configuration contain several thousand lines of tightly connected behavior. Do not start with a rewrite. First capture constructor selection, field resolution, constraint resolution, collection construction, semantic adjustment, and error reporting as black-box contracts. Then extract one responsibility at a time with tests green after every change.

### P1.3 Replace coverage theatre with contract confidence

The 99.9% gate is impressive, but dozens of tests access private implementation details or exist primarily to satisfy coverage. Keep strong line/branch coverage, then add:

- Mutation testing for random bounds, constraints, nullability, locale fallback, escaping, and seed propagation.
- Revapi/japicmp compatibility tests.
- JPMS consumer tests, including documented `opens` requirements for reflection.
- Java 21 plus current LTS/latest CI.
- Kotlin immutable-class, Kotest replay, Spring slice, and published-artifact smoke tests.
- Strict compiler warnings and a focused static analyzer.

### P1.4 Publish a BOM and remove accidental Java-consumer cost

The Java-only core module has no Kotlin sources but publishes a runtime dependency on `kotlin-stdlib`. Remove it. Publish a `krandom-bom` for the synchronized artifacts so adopters cannot accidentally mix versions. Keep the current module topology unless real dependency/adoption measurements justify splitting object generation from core.

### P1.5 Measure performance honestly

Scalar generators are fast, but object generation is hundreds of times slower than several benchmark competitors in the current dashboard. The comparison is not equivalent—kRandom performs reflection and semantic coherence while one competitor benchmark manually assigns fields—so neither a blanket “faster” claim nor a raw league table is fair.

Use at least three forks, record confidence intervals, define budgets for representative scalar/object/schema workloads, and publish the full table with the semantic-work caveat. Optimize measured bottlenecks; do not optimize to a marketing headline.

### P1.6 Finish the schema contract

Document the exact supported subset of JSON Schema/OpenAPI. Decide whether metadata-provider failures are fatal or explicitly unknown instead of silently producing an unconstrained schema. Add contract tests for references, compositions, enums, patterns, numeric/string bounds, formats, and recursive types before expanding parser scope.

## Useful after v2 stability — P2

- Add more providers/locales only after the provider catalog and validity metadata exist.
- Consider advanced schema composition only after the supported subset is stable.
- Consider module splitting only if consumer dependency data shows a real problem.
- Improve `UniqueGenerator`'s default equality path with a set and define exhaustion/memory behavior.
- Add richer JUnit seed injection and a global replay property/environment variable.
- Publish a machine-readable generation recipe and dataset versions for cross-project fixtures.
- Add observability hooks for fixture generation failures without logging generated personal-looking data.

## Work that should not enter v2 scope

- New facade aliases for competitor source compatibility.
- More locale/provider breadth before existing registries are safe and scoped.
- A wholesale object-generator rewrite.
- Performance claims based only on scalar throughput.
- “Supports any class” language while constructor bypass and JPMS access have restrictions.
- Organization-wide migration before RC pilots prove compatibility and replay.

## Recommended delivery sequence

### Phase 1: 1.6.x bridge and contract freeze

**Goal:** Stop API growth and give existing users a migration path.
**Deliverables:** API inventory, deprecations, corrected documentation, Kotlin dependency removal, BOM, compatibility gate, known-limitations page.
**Exit gate:** Latest 1.6.x passes all existing checks and every planned v2 removal has a documented replacement.

### Phase 2: v2 correctness foundation

**Goal:** Fix object validity, random ownership, diagnostics, and registry isolation.
**Deliverables:** safe construction policy, recursive type model, checked constraint model, deterministic child streams, generation recipe, typed provider catalog.
**Exit gate:** P0.1, P0.2, and P0.6 acceptance tests pass without ignored errors.

### Phase 3: v2 integrations and release engineering

**Goal:** Make adapters reproducible and publication recoverable.
**Deliverables:** Kotest replay, typed Kotlin DSL, real Spring slice, JPMS consumers, verified build inputs, tag-driven release, artifact smoke tests.
**Exit gate:** Every integration passes tests against locally published artifacts, not project-classpath shortcuts.

### Phase 4: v2 RC pilots

**Goal:** Prove the contract in real projects before GA.
**Deliverables:** RC BOM, migration guide, two representative pilots, issue log, rollback instructions, performance baseline.
**Exit gate:** Two consecutive RCs with no P0 regression and no unexplained seed, fixture-validity, or compatibility failure.

### Phase 5: v2 GA and controlled adoption

**Goal:** Roll out a pinned, observable release without a big bang.
**Deliverables:** GA artifacts, signed release notes, support policy, adoption template, compatibility matrix.
**Exit gate:** Post-publication checks pass from Maven Central and the canary projects remain green.

## Adoption plan for other projects

1. **Inventory:** find every hand-written faker, fixture factory, random utility, Easy Random/DataFaker/Instancio use, property-test adapter, locale requirement, and seed/replay mechanism. Do not change code yet.
2. **Classify:** group projects by language, framework, build tool, JPMS use, fixture volume, and whether generated data reaches snapshots, persisted tests, or external systems.
3. **Pilot:** choose one plain-Java library and one Spring/Kotlin application. Import the BOM, pin one recipe, and migrate a narrow fixture boundary behind an existing project interface.
4. **Compare:** run old and new fixtures in parallel where practical. Measure failure diagnostics, test flakiness, execution time, constraint validity, and replay success.
5. **Expand:** migrate one bounded fixture domain per change. Keep the old adapter for one release and make rollback a dependency/configuration change.
6. **Standardize:** publish an internal adoption guide with approved profiles, seeds, clocks, locale policy, safety modes, and forbidden uses such as production identity/payment generation.

Each project must pass its own test suite and replay a deliberately failing seeded case before adoption is considered complete. Do not expose kRandom directly throughout application code; keep a small project-owned fixture interface so replacement and version changes remain reversible.

## v2 GA checklist

- [ ] All P0 acceptance criteria pass.
- [ ] Public API inventory and binary/source baseline are enforced.
- [ ] Patch-level seed compatibility has golden tests and a documented algorithm version.
- [ ] Object fixtures are valid or fail with a full field path; no silent partial values.
- [ ] Kotlin immutable classes, nested generics, Jakarta Validation, JPMS, Kotest replay, and standalone Spring slice have consumer tests.
- [ ] Registries and provider metadata are context-scoped and single-source.
- [ ] BOM and all artifacts resolve from Maven Central in clean Maven and Gradle builds.
- [ ] Wrapper, CI actions, and downloaded tools are pinned and verified; SBOM and provenance are published.
- [ ] Documentation versions, support counts, examples, and validity claims are checked automatically.
- [ ] Benchmarks are reproducible, balanced, and have regression budgets.
- [ ] Two RC pilot projects pass with documented rollback.
- [ ] Release recovery has been rehearsed without publishing a duplicate version.

## Final recommendation

Treat the current branch as a strong late-1.x development line, not a v2 release candidate. Cut a focused 1.6.x bridge, then make v2 about **valid fixtures, deterministic recipes, a smaller stable API, honest integrations, and recoverable releases**. Once those contracts pass in two real pilots, organization-wide adoption becomes a routine migration instead of a gamble.
