# kRandom v2 Master Implementation Plan

**Created:** 2026-07-09
**Status:** Ready for execution
**Source review:** [`../reviews/v2-release-readiness-review-2026-07-09.md`](../reviews/v2-release-readiness-review-2026-07-09.md)
**Current line:** `1.6.0-SNAPSHOT`
**Target:** A stable v2 contract followed by controlled adoption across consumer projects

## 1. Purpose

This is the single source of truth for preparing, releasing, and adopting kRandom v2. It converts the v2 readiness audit into ordered work packages with dependencies, implementation steps, tests, and objective completion gates.

Historical parity, roadmap, and release plans remain useful research, but they do not control v2 scope. If they conflict with this plan, this plan wins.

The governing principle is simple:

> v2 is a contract release: valid fixtures, deterministic replay, explicit failures, a smaller stable API, honest integrations, and a recoverable release process.

It is not a generator-count release. New providers, aliases, and speculative abstractions stay out until the v2 contract is stable.

## 2. Outcomes

At the end of this plan:

- Object generation either returns a valid fixture or reports a contextual failure; it never silently returns a partially initialized object by default.
- Seeded generation has one documented, tested replay model across scalar, object, schema, JUnit, jqwik, Kotest, Kotlin, and Spring use.
- Public APIs are inventoried, intentionally versioned, and protected by binary/source compatibility checks.
- Kotlin immutable types, nested generics, Jakarta Validation constraints, JPMS consumers, and framework adapters have explicit supported behavior.
- Financial and identity generators state and enforce their safety and validity level.
- Provider keys, aliases, metadata, locale fallback, and registry scope have one source of truth.
- Documentation is generated or checked against current code facts.
- Releases are reproducible, verifiable, resumable after partial failure, and smoke-tested from Maven Central.
- At least two real consumer projects prove the RC before GA.
- Organization-wide adoption proceeds in reversible waves rather than a big bang.

## 3. Stakeholders and requirements

### Library consumers

Java and Kotlin developers need fixtures that are easy to compose, deterministic when seeded, valid enough for their stated purpose, and clear when unsupported.

### Test-framework consumers

JUnit, jqwik, and Kotest users need kRandom to honor each framework's seed, replay, shrinking, and lifecycle conventions instead of hiding a separate mutable random stream.

### Framework consumers

Spring and Jackson users need integrations whose annotations, defaults, configuration, and published dependencies match their documentation.

### Maintainers

Maintainers need a public API baseline, small reviewable changes, reliable diagnostics, single-source metadata, and release recovery that does not require republishing immutable versions.

### Adopting projects

Projects migrating from hand-written fixtures or competing libraries need a BOM, migration recipes, stable profiles, observable regressions, and a dependency-level rollback.

### Safety and compliance reviewers

Reviewers need to distinguish format-valid, checksum-valid, semantically plausible, and deliberately non-routable test data. They also need verified build inputs, SBOMs, and provenance.

## 4. Scope

### Required for v2 GA

- Every P0 work package in this plan.
- P1 work that protects correctness, compatibility, integration behavior, publication, or migration.
- Two RC pilots and one rehearsed rollback.
- Maven Central verification using clean Maven and Gradle consumers.

### Explicitly deferred until after v2 stability

- New provider families and locale expansion.
- New parity aliases.
- A wholesale object-generation rewrite.
- Module splitting without measured consumer evidence.
- Full JSON Schema/OpenAPI feature parity beyond the documented supported subset.
- Performance optimization without a representative benchmark and regression budget.

## 5. Working rules

Every implementation step follows the same flow:

1. **Understand:** inspect current source, tests, Javadocs, documentation, and consumer examples.
2. **Specify:** record the public behavior and decision before changing implementation.
3. **Red:** add the smallest behavior test that fails for the identified gap.
4. **Green:** make the minimum implementation change that passes it.
5. **Refactor:** simplify only after the focused and existing tests pass.
6. **Verify:** run the smallest relevant task, then the full Java 21 pre-commit gate when the work package is complete.
7. **Document:** update Javadocs, migration notes, configuration tables, and this plan's status in the same change.
8. **Commit:** propose a small Conventional Commit and ask the user before creating it. Never add assistant attribution.

Additional rules:

- Do not keep a knowingly failing test on the shared branch.
- Do not disable or weaken tests to land a change.
- Do not silently catch fixture-construction errors in strict/default mode.
- Do not combine unrelated API, behavior, and build changes in one commit.
- Stop after three failed implementation attempts on one issue, record the evidence, and reassess the abstraction.
- `JAVA_HOME=<JDK 21+> ./scripts/pre_commit_check.sh` must pass before every proposed commit.
- Run `./scripts/verify_examples_local.sh` for public API, artifact, dependency, or integration changes.

## 6. Decision gates

These decisions must be recorded before their dependent implementation begins. The recommended defaults are included so work can proceed without reopening settled questions accidentally.

| ID | Decision | Recommended default | Blocks |
|---|---|---|---|
| D1 | Default construction policy | Constructors and records are safe/default; constructor bypass is explicit unsafe opt-in | 2.4, 2.5 |
| D2 | Kotlin immutable support | Implement primary-constructor generation in the Kotlin module; core must fail clearly when support is unavailable | 2.5, 3.4 |
| D3 | Random-source combinations | Model seed, caller source, secure source, and factory as mutually exclusive source specifications; reject conflicts | 2.6 |
| D4 | Replay compatibility | Patch releases preserve versioned golden streams; minor/major changes require an algorithm/recipe version | 2.7, 4.1 |
| D5 | Bounds | Reject `min >= max` consistently; never silently swap caller mistakes | 3.2 |
| D6 | Default error behavior | Strict fail-fast default; resilience only through explicit `ignoreErrors`/lenient configuration | 2.1–2.5 |
| D7 | Financial/identity safety | Non-routable or official test data by default; production-like checksum-valid data is opt-in | 2.9 |
| D8 | Global registries | Context-scoped registries are canonical; global mutation is a deprecated 1.6 bridge only | 2.8 |
| D9 | v2 API removals | Remove only after a 1.6 deprecation and migration path, except behavior that cannot safely remain | 1.4, 1.5, 3.1 |
| D10 | Supported schema scope | Publish an explicit supported subset; unsupported constructs fail or report `unknown`, never disappear silently | 3.7 |

## 7. Dependency order

The critical path is:

```text
1.6 contract freeze
  -> diagnostics and type/constraint correctness
  -> construction and random contracts
  -> provider/safety contracts
  -> integrations and compatibility matrix
  -> v2 alpha/RC
  -> two real-project pilots
  -> v2 GA
  -> controlled adoption waves
```

Work may run in parallel only when the dependency table for its stage allows it. Passing tests in one lane does not waive a stage exit gate.

---

## Stage 1: Freeze the Contract and Ship the 1.6 Bridge

**Goal**: Stop uncontrolled API growth, establish compatibility tooling, correct public claims, and give current consumers a migration path before v2 removes or changes behavior.
**Success Criteria**: A released 1.6.x bridge contains the v2 deprecations, BOM, accurate documentation, compatibility baseline, and supply-chain baseline without introducing v2 breaking behavior.
**Tests**: API baseline comparison against 1.5.0, full pre-commit gate, all consumer examples, generated POM inspection, documentation facts checks, and a release dry-run.
**Status**: In Progress

### Step 1.1 — Establish the v2 backlog and change protocol

**Depends on:** Nothing.

**Actions**

- [ ] Convert every numbered work package in this plan into one trackable issue or equivalent backlog item.
- [ ] Create milestones for `1.6 bridge`, `v2 foundation`, `v2 integrations`, `v2 RC`, and `v2 GA`.
- [ ] Add labels for `contract`, `correctness`, `compatibility`, `integration`, `release`, `documentation`, `performance`, and `adoption`.
- [ ] Link each item to its decision gate, acceptance tests, and dependent work packages.
- [ ] Record a short decision note when D1–D10 are accepted or changed.
- [ ] Keep this document authoritative for ordering and stage status.

**Tests**

- [ ] Every audit P0/P1 finding maps to at least one work package and one milestone.
- [ ] No issue has an undefined success criterion.

**Done when:** The full v2 scope is visible, traceable, ordered, and has no orphaned release blocker.

### Step 1.2 — Generate and classify the public API inventory

**Depends on:** 1.1.

**Actions**

- [x] Generate the exported class, method, constructor, field, annotation, and configuration surface for every published artifact.
- [x] Record `Generators` facade methods by canonical operation and alias family.
- [x] Classify each API as `keep`, `deprecate in 1.6`, `remove in v2`, `rename`, `internalize`, or `requires decision`.
- [x] Record replacement examples for every deprecation.
- [x] Identify public Javadocs that reference package-private types.
- [x] Check Maven/Gradle coordinates and automatic module names as part of the public contract.

**Tests**

- [x] The inventory can be regenerated deterministically in CI.
- [x] A review script fails when an unclassified public API appears.
- [x] All current facade methods are accounted for by explicit exceptions plus the default **KEEP** disposition.

**Done when:** Every exported symbol has an intentional v2 disposition and owner.

### Step 1.3 — Add binary and source compatibility enforcement

**Depends on:** 1.2.

**Actions**

- [x] Select Revapi or japicmp using the smallest integration that covers all published Java artifacts.
- [x] Baseline 1.6 development against the Maven Central `1.5.0` artifacts.
- [x] Fail CI on unapproved binary/source incompatibility.
- [x] Add a reviewed allowlist format for intentional v2 removals; do not use broad package exclusions.
- [x] Add the compatibility task to the local pre-release check and release workflow.
- [x] Document what the tool cannot detect, including behavioral and serialized-data changes.

**Tests**

- [x] A deliberate public method removal makes the compatibility task fail.
- [x] A deprecation without removal remains compatible.
- [x] All published modules are included.

**Done when:** Accidental API breaks cannot reach a release unnoticed.

### Step 1.4 — Add the 1.6 deprecation bridge

**Depends on:** D9, 1.2, 1.3.

**Actions**

- [x] Deprecate redundant facade aliases and point to one canonical name per operation.
- [ ] Deprecate global registry mutations that will be replaced by scoped contexts.
- [ ] Deprecate ambiguous reseeding fallbacks that cannot preserve the v2 contract.
- [x] Add `@since`, `@Deprecated(since = "1.6", forRemoval = true)`, and migration examples.
- [x] Keep deprecated methods as thin delegates; do not duplicate behavior.
- [x] Add a v1.6-to-v2 migration table organized by common use case.

**Tests**

- [x] Deprecated and canonical entry points return equivalent values under the same configuration.
- [x] Javadocs contain valid replacement links.
- [x] Consumer examples compile with deprecation warnings enabled.

**Done when:** Every planned safe removal has a working 1.6 replacement path.

### Step 1.5 — Create one documentation facts source

**Depends on:** 1.1.

**Actions**

- [x] Define machine-readable facts for latest GA version, development version, module list, Java minimum, native locale count, fallback locale count, total locale variants, and supported constraints/formats.
- [x] Replace stale hard-coded `1.0.0` snippets in current guides.
- [x] Correct locale contribution resource paths.
- [x] Correct JUnit introduction history and Spring default-random claims.
- [x] Introduce precise validity terms: `format-valid`, `checksum-valid`, `semantically plausible`, and `test-safe/non-routable`.
- [x] Generate or test README/docs-site tables from the facts source.
- [x] Compile executable documentation snippets where practical.

**Tests**

- [x] A facts test compares documentation counts with enums/resources.
- [x] A version check detects stale current-version snippets.
- [x] All docs-site links and code snippets pass.

**Done when:** Public documentation cannot silently drift from code facts.

### Step 1.6 — Remove accidental Java-consumer dependencies and publish a BOM

**Depends on:** 1.3.

**Actions**

- [x] Remove the Kotlin plugin/runtime dependency from `core`, which contains no Kotlin sources.
- [x] Inspect generated POM and Gradle metadata for every artifact.
- [x] Include the project license in each published binary/source artifact and verify its path.
- [x] Add a `krandom-bom` Java platform containing all synchronized artifact versions.
- [x] Update Maven and Gradle installation examples to use the BOM where multiple modules are consumed.
- [x] Add a mixed-version rejection/compatibility test using consumer builds.
- [x] Keep existing module boundaries unless measured evidence justifies a split.

**Tests**

- [x] A Java-only consumer resolves core without `kotlin-stdlib`.
- [x] Maven and Gradle consumers import the BOM and omit module versions.
- [x] Published metadata contains the intended dependency scopes only.

**Done when:** Java consumers pay no accidental Kotlin cost and multi-module consumers have one version source.

### Step 1.7 — Establish the build and supply-chain baseline

**Depends on:** 1.1.

**Actions**

- [x] Add the Gradle wrapper distribution SHA-256 checksum.
- [x] Enable Gradle dependency verification and document the update procedure.
- [x] Evaluate dependency locking for build/test configurations and enable it where it improves reproducibility without constraining library consumers.
- [x] Pin GitHub Actions to immutable commit SHAs.
- [x] Pin and verify the downloaded Mill launcher and any other executed binary.
- [ ] Resolve or upgrade the Central publication plugin before its deprecation becomes a Gradle 10 error.
- [ ] Generate CycloneDX or SPDX SBOMs for release artifacts.
- [ ] Generate build provenance/attestation in the release workflow.
- [x] Add strict dependency/repository checks that reject dynamic versions and unexpected repositories.

**Tests**

- [ ] Wrapper verification fails with a deliberately wrong checksum.
- [x] Dependency verification runs in CI and clean local builds.
- [x] No workflow action or downloaded executable is floating/unverified.
- [x] `./gradlew help --warning-mode all` has no project-controlled Gradle 10 blocker.

The remaining Gradle 10 deprecation is attributed by Gradle's problems report to
`com.gradleup.nmcp.settings` 1.6.1. Keep the publication-plugin action open until an upstream fix or
tested replacement removes it.

**Done when:** Every build input used for a release is pinned or cryptographically verified.

### Step 1.8 — Publish and validate the 1.6 bridge

**Depends on:** 1.3–1.7.

**Actions**

- [ ] Freeze the 1.6 changelog around migration enablement and corrected contracts.
- [ ] Run the full pre-commit and consumer-example suites from a clean checkout.
- [ ] Run API compatibility against 1.5.0.
- [ ] Inspect POMs, module metadata, signatures, sources, Javadocs, BOM, SBOM, and provenance.
- [ ] Publish using the current recoverable flow or complete Step 3.11 first if the current flow cannot be safely recovered.
- [ ] Smoke-test every artifact from Maven Central, not `mavenLocal()`.
- [ ] Announce the deprecation window and v2 migration guide.

**Tests**

- [ ] Clean Maven and Gradle consumers resolve 1.6 from Central.
- [ ] Existing 1.5-style examples still compile and run through deprecated delegates.
- [ ] Canonical 1.6 examples compile without deprecation warnings.

**Done when:** 1.6 is a stable bridge that consumers can adopt before v2.

---

## Stage 2: Build the v2 Correctness Foundation

**Goal**: Make object creation, constraints, random sources, registries, and sensitive data explicit and correct before higher-level adapters depend on them.
**Success Criteria**: Default generation returns valid supported values or one contextual exception; all P0 core-contract tests pass; deterministic recipes replay across core generation paths.
**Tests**: Focused red/green unit tests, black-box object fixtures, Hibernate Validator integration, golden seed streams, concurrency/replay tests, resource/registry tests, and the full pre-commit gate.
**Status**: Not Started

### Step 2.1 — Introduce a contextual generation failure model

**Depends on:** D6, Stage 1.

**Actions**

- [ ] Inventory every swallowed `RuntimeException`, reflection failure, collection insertion failure, and schema metadata fallback.
- [ ] Define a small failure hierarchy with operation, owner type, field/schema path, declared type, depth, and root cause.
- [ ] Make strict behavior the default.
- [ ] Route explicit lenient behavior through one policy rather than scattered catch blocks.
- [ ] Prevent exception messages from printing generated personal-looking values.
- [ ] Define an optional sanitized diagnostic event/listener containing failure category, path, and replay-recipe identity without generated personal-looking values.
- [ ] Preserve causes and add context at the correct boundary rather than wrapping repeatedly.

**Tests**

- [ ] Map, queue, array, constructor, reflection, JPMS-access, and custom-generator failures report full paths.
- [ ] Strict mode never returns a partial fixture after an assignment failure.
- [ ] Lenient mode has explicit, documented fallback assertions.
- [ ] Diagnostic listeners receive structured sanitized failures and cannot observe generated field values by default.

**Done when:** Unsupported or failed generation is diagnosable without debugging internal reflection code.

### Step 2.2 — Replace shallow generic handling with a recursive type model

**Depends on:** 2.1.

**Actions**

- [ ] Characterize the current `Class<?>`/`Type` conversion paths.
- [ ] Model classes, parameterized types, generic arrays, wildcards, and type variables recursively.
- [ ] Carry annotations and the full field path into nested resolution.
- [ ] Define supported wildcard/type-variable bounds and fail clearly for unsupported ambiguity.
- [ ] Reuse one resolver for fields, records, constructors, collections, maps, arrays, and schema inference.
- [ ] Remove silent conversion to `Object.class` for nested parameterized types.

**Tests**

- [ ] Cover `List<List<String>>`, `Map<String,List<Integer>>`, nested optionals, generic arrays, upper/lower wildcards, inherited type variables, and recursive types.
- [ ] Cover equivalent Java records and Kotlin generic data classes.
- [ ] Unsupported type shapes include the complete path and type signature.

**Done when:** Nested generic values are generated with their declared element types or rejected explicitly.

### Step 2.3 — Normalize and validate Bean Validation constraints

**Depends on:** 2.1, 2.2.

**Actions**

- [ ] Build one normalized constraint model for nullability, size, numeric range, sign, time, pattern, email, and assertion constraints.
- [ ] Add `@NotNull` and `@NotEmpty` alongside existing `@NotBlank` support.
- [ ] Intersect multiple compatible constraints before generation.
- [ ] Detect impossible intersections before producing a value.
- [ ] Define precedence between explicit field rules, annotations, defaults, and null probability.
- [ ] Validate advertised output with a real Jakarta Validation implementation in tests.
- [ ] Publish the exact supported-constraint table from code facts.

**Tests**

- [ ] Add red/green cases for every supported annotation, boundary, composition, and nullability interaction.
- [ ] Prove `@Min(10) @Max(5)` and `@Size(min=5,max=2)` fail fast.
- [ ] Prove null probability cannot violate `@NotNull`, `@NotEmpty`, or primitive requirements.
- [ ] Validate generated objects through Hibernate Validator across many deterministic seeds.

**Done when:** Generated fixtures satisfy every advertised constraint or generation fails before returning them.

### Step 2.4 — Make Java object construction safe and explicit

**Depends on:** D1, 2.1–2.3.

**Actions**

- [ ] Specify constructor selection for records, explicit constructors, no-argument beans, factory hooks, and unsupported classes.
- [ ] Prefer canonical/declared constructors where parameters can be resolved.
- [ ] Move Objenesis/constructor bypass behind an explicit unsafe policy.
- [ ] Detect final/unwritable fields before creating an instance that cannot be completed.
- [ ] Respect default field initialization unless an explicit override policy says otherwise.
- [ ] Document required JPMS `opens` behavior until Step 3.8 provides full consumer coverage.

**Tests**

- [ ] Cover records, immutable constructor classes, mutable beans, inherited fields, final fields, private constructors, abstract/interface types, inner classes, cycles, and factory overrides.
- [ ] Prove constructor invariants run in safe mode.
- [ ] Prove unsafe mode is opt-in and named in diagnostics/recipes.

**Done when:** “Generated object” means “constructed under a documented policy,” not merely allocated memory.

### Step 2.5 — Support immutable Kotlin types without silent corruption

**Depends on:** D2, 2.2–2.4.

**Actions**

- [ ] Keep Kotlin reflection/runtime support in the Kotlin module, not Java core.
- [ ] Resolve the primary constructor, parameter names, nullability, defaults, optional parameters, and generic types.
- [ ] Feed constructor parameters through the same constraints, provider rules, and type model as Java.
- [ ] Make core fail with a clear support message when it encounters an immutable Kotlin class without the Kotlin integration.
- [ ] Define behavior for value classes, sealed types, objects, secondary constructors, and delegated properties.
- [ ] Correct KDoc and examples to match actual support.

**Tests**

- [ ] Cover data classes with `val`, nullable/non-null types, defaults, nested generics, annotations, value classes, and constructor validation.
- [ ] Assert that no non-null Kotlin property contains a runtime null.
- [ ] Add locally published Kotlin consumer tests, not only module-classpath tests.

**Done when:** Immutable Kotlin objects are validly constructed or rejected before a value escapes.

### Step 2.6 — Replace random precedence with an explicit source contract

**Depends on:** D3, 2.1.

**Actions**

- [ ] Enumerate seed, caller random, secure source, algorithm factory, and default-source configurations.
- [ ] Replace ambiguous precedence with mutually exclusive source specifications or builder validation.
- [ ] Define source ownership, lifecycle, thread behavior, and whether a source is reproducible.
- [ ] Make scalar, object, schema, and provider entry points use the same source creation path.
- [ ] Correct Javadocs about `Random` thread safety, contention, interleaving, and reproducibility.
- [ ] Prevent configuration from claiming secure mode when it creates a non-secure source.

**Tests**

- [ ] Implement a truth-table test for every allowed and rejected source combination.
- [ ] Prove equivalent configurations behave consistently across generator families.
- [ ] Add concurrent-use tests that distinguish safety from reproducibility.
- [ ] Prove caller-owned sources are not copied or reseeded implicitly.

**Done when:** A consumer can predict the source, ownership, security, and replay behavior from configuration alone.

### Step 2.7 — Add versioned deterministic recipes and child streams

**Depends on:** D4, 2.6.

**Actions**

- [ ] Define `GenerationRecipe` fields: library version, recipe version, algorithm, seed, locale, clock, profile, safety policy, construction policy, and provider dataset version.
- [ ] Make recipes serializable in a stable, human-readable form.
- [ ] Derive named child streams for object fields and schema columns so unrelated additions do not reorder all downstream values.
- [ ] Define how repeated fields, lists, maps, recursion, and parallel generation derive stream identities.
- [ ] Make failure messages and test integrations print a safe replay recipe.
- [ ] Add an explicit compatibility policy for recipe/algorithm changes.

**Tests**

- [ ] Add checked-in golden streams per representative generator family.
- [ ] Prove recipes replay scalar, object, and schema outputs.
- [ ] Prove adding an unrelated object field does not perturb existing named fields where the contract promises stability.
- [ ] Prove locale, clock, provider data, and safety-policy changes appear in the recipe.

**Done when:** A failed fixture can be reproduced from one portable recipe rather than hidden mutable state.

### Step 2.8 — Unify provider catalogs and registry scope

**Depends on:** D8, 2.1, 2.6.

**Actions**

- [ ] Inventory canonical provider keys, aliases, result types, factories, locale support, fallback, validation, and metadata across `ProviderHub`, `FieldLookup`, `SemanticFieldRegistry`, and all data registries.
- [ ] Define one typed provider descriptor/catalog.
- [ ] Generate lookup, semantic inference, schema metadata, and documentation from the catalog.
- [ ] Move every provider registry into `DataRegistryContext` or its v2 replacement.
- [ ] Validate registered keys and data arrays uniformly.
- [ ] Return immutable snapshots rather than live unmodifiable concurrent-map views.
- [ ] Keep deprecated global registration as a 1.6 adapter only.

**Tests**

- [ ] A catalog completeness test finds missing/duplicate keys and alias collisions.
- [ ] Every provider can be overridden in one context without affecting another.
- [ ] Locale fallback and registry validation are uniform across old and new provider families.
- [ ] Concurrent context tests show no global leakage.

**Done when:** Provider identity, metadata, and customization have one source of truth and no hidden global state.

### Step 2.9 — Add explicit financial and identity safety modes

**Depends on:** D7, 2.7, 2.8.

**Actions**

- [ ] Classify credit card, bank, IBAN, national ID, phone, crypto, and similar generators by validity and routability.
- [ ] Select official processor test values/ranges or deliberately non-routable shapes where standards provide them.
- [ ] Make production-like checksum-valid output an explicit opt-in policy.
- [ ] Add provider metadata for format validity, checksum validity, semantic plausibility, and test safety.
- [ ] Include the safety policy in generation recipes and schema metadata.
- [ ] Add prominent forbidden-use guidance for production identity, payment, KYC, or account creation.

**Tests**

- [ ] Default payment fixtures stay inside approved test/non-routable ranges.
- [ ] Opt-in validation-valid modes satisfy their advertised algorithms.
- [ ] Documentation and metadata match implementation for every classified provider.

**Done when:** Consumers cannot confuse “passes a validator” with “safe test credential.”

### Step 2.10 — Run the foundation integration gate

**Depends on:** 2.1–2.9.

**Actions**

- [ ] Run all focused correctness suites and the full pre-commit gate.
- [ ] Run Java/Kotlin consumer examples against locally published artifacts.
- [ ] Compare public API changes with the 1.6 baseline.
- [ ] Review every new exception and migration note for clarity.
- [ ] Update Stage 2 decisions and unresolved limitations.

**Tests**

- [ ] All P0.1, P0.2, and P0.6 acceptance criteria from the audit pass.
- [ ] No ignored assignment/insertion failure remains in default object generation.
- [ ] Reproducibility, validity, and safety tests pass across repeated clean runs.

**Done when:** Higher-level integrations can build on a stable, explicit core contract.

---

## Stage 3: Stabilize the API, Integrations, Quality, and Release System

**Goal**: Complete the v2 public surface, make every adapter honor its host framework, validate real module boundaries, and make publication recoverable.
**Success Criteria**: All published modules pass their host-framework contract tests and compatibility gates; release automation can resume safely; documentation and performance claims are evidence-backed.
**Tests**: Compatibility baseline, integration replay tests, published-artifact consumers, JPMS tests, mutation testing, multi-JDK CI, benchmark regression checks, release dry-runs, and Maven Central smoke simulations.
**Status**: Not Started

### Step 3.1 — Simplify the v2 API and object configuration

**Depends on:** Stage 2, 1.2–1.4.

**Actions**

- [ ] Remove only APIs classified for v2 removal after the 1.6 bridge exists.
- [ ] Keep one canonical name and consistent overload pattern for each operation.
- [ ] Split the giant facade only into small domain namespaces justified by the inventory; avoid speculative abstraction.
- [ ] Collapse duplicated `ObjectGeneratorConfig` state into `GeneratorConfig` or one composed public object policy.
- [ ] Make configuration structurally immutable by defensively copying/wrapping mutable collections and clearly documenting ownership of caller callbacks/factories.
- [ ] Remove public documentation references to package-private types.
- [ ] Generate the new v2 API baseline.

**Tests**

- [ ] Migration examples compile against v2.
- [ ] No removed API lacks a 1.6 replacement entry.
- [ ] Configuration round-trip/copy tests prove no state is lost or duplicated.
- [ ] API analysis detects accidental reintroduction of removed aliases.

**Done when:** The v2 surface is smaller, consistent, documented, and mechanically protected.

### Step 3.2 — Finish core combinator and boundary contracts

**Depends on:** D5, 2.6, 3.1.

**Actions**

- [ ] Make `map`, `filter`, and other decorators preserve seed/replay behavior when their source supports it.
- [ ] Replace runtime-optional reseeding with a clearer type/adapter contract where possible.
- [ ] Enforce strict bound semantics consistently.
- [ ] Optimize the default equality path of `UniqueGenerator` with a set and define memory/exhaustion behavior.
- [ ] Review selection, weighted, shuffle, retry, and uniqueness combinators for overflow and non-termination policies.

**Tests**

- [ ] Composed generators replay from recipes.
- [ ] Equal/reversed bounds fail with consistent messages.
- [ ] Uniqueness has deterministic exhaustion tests and does not degrade quadratically on ordinary values.

**Done when:** Generator composition does not discard the contracts established in Stage 2.

### Step 3.3 — Make Kotest replay and shrinking honest

**Depends on:** 2.5–2.7, 3.1.

**Actions**

- [ ] Adapt Kotest `RandomSource` into per-case kRandom generation.
- [ ] Eliminate hidden mutable unseeded streams from `Arb` adapters.
- [ ] Define meaningful edge cases and shrinkers for bounded primitives and selections.
- [ ] Explicitly document types that cannot be structurally shrunk.
- [ ] Print a kRandom recipe alongside Kotest seed information on failure.
- [ ] Test the published adapter with the supported Kotest version range.

**Tests**

- [ ] Re-running a failing Kotest seed reproduces the same fixture sequence.
- [ ] Shrinking converges for supported primitives and selections.
- [ ] Parallel property tests do not share mutable generator state.

**Done when:** Kotest users can rely on Kotest's normal replay workflow.

### Step 3.4 — Make the Kotlin DSL typed and aligned

**Depends on:** 2.5, 3.1.

**Actions**

- [ ] Add `KProperty1`-based field rules and keep string rules only as an explicit compatibility bridge if needed.
- [ ] Validate duplicate, unused, incompatible, and unknown rules at DSL build time.
- [ ] Align Kotlin defaults with Java defaults unless a documented Kotlin-specific behavior is intentional.
- [ ] Expose safe construction and generation recipes idiomatically.
- [ ] Add examples for immutable data classes, nested generics, defaults, and constraints.

**Tests**

- [ ] Property-reference rules survive Kotlin refactoring/compiler checks.
- [ ] Invalid rules fail before generation.
- [ ] Java and Kotlin configurations produce equivalent documented behavior.

**Done when:** The Kotlin DSL is type-safe, predictable, and no longer implies unsupported behavior.

### Step 3.5 — Make `@KrandomTest` a real Spring test slice

**Depends on:** 2.6–2.8, 3.1.

**Actions**

- [ ] Compose the required Spring test extension/bootstrap annotations.
- [ ] Limit the context to the documented kRandom beans.
- [ ] Align property defaults and metadata with core configuration facts.
- [ ] Verify property binding for seed/recipe, locale, clock, safety, and construction policies.
- [ ] Add failure diagnostics for invalid property combinations.
- [ ] Test against the supported Spring Boot line using published artifacts.

**Tests**

- [ ] A consumer test using only `@KrandomTest` starts the promised context.
- [ ] Full `@SpringBootTest` and slice tests produce equivalent configured generators.
- [ ] Invalid properties fail fast with actionable messages.

**Done when:** The annotation's example works exactly as written.

### Step 3.6 — Complete JUnit and jqwik replay integration

**Depends on:** 2.7, 3.1.

**Actions**

- [ ] Add a global system property/environment variable for replay recipes.
- [ ] Allow recipe/seed injection without editing test source.
- [ ] Preserve string-seed metadata instead of reducing it invisibly to a number.
- [ ] Bridge jqwik random context where its API permits and document limitations.
- [ ] Print one copyable replay command on failure.

**Tests**

- [ ] A deliberately failing test replays via CLI property.
- [ ] Parallel and parameterized tests get isolated deterministic sources.
- [ ] JUnit lifecycle callbacks do not leak state between tests.

**Done when:** A CI failure contains everything needed for local replay.

### Step 3.7 — Define and enforce the schema contract

**Depends on:** D10, 2.2, 2.3, 2.8, 3.1.

**Actions**

- [ ] Publish the supported JSON Schema/OpenAPI subset.
- [ ] Decide explicit behavior for `$ref`, compositions, recursion, enums, patterns, bounds, formats, and unknown provider metadata.
- [ ] Replace silent metadata exceptions with contextual failure or explicit `unknown` state.
- [ ] Reuse the recursive type and provider metadata models.
- [ ] Define schema generator thread/reuse behavior.
- [ ] Keep streaming export guarantees and escaping behavior intact.

**Tests**

- [ ] Contract fixtures cover every supported and unsupported construct.
- [ ] Round-trip tests validate supported imports/exports.
- [ ] JSONL, CSV, XML, and SQL escaping tests remain green.
- [ ] Unsupported constructs never become silently unconstrained schemas.

**Done when:** Schema support is precise, testable, and no broader than implementation.

### Step 3.8 — Add JPMS and strong-encapsulation consumers

**Depends on:** 2.4, 3.1.

**Actions**

- [ ] Decide whether automatic module names remain sufficient or explicit descriptors are warranted.
- [ ] Add named-module consumers for core and each integration.
- [ ] Document required `opens` clauses for reflection-based object generation.
- [ ] Fail with a JPMS-specific diagnostic when access is denied.
- [ ] Verify split packages and module names across all artifacts.

**Tests**

- [ ] Named-module examples compile and run on the minimum and current tested JDKs.
- [ ] Missing `opens` produces the documented actionable error.
- [ ] Artifact module names remain stable under compatibility checks.

**Done when:** JPMS use is supported explicitly rather than accidentally.

### Step 3.9 — Upgrade quality gates from coverage to contract confidence

**Depends on:** 2.10.

**Actions**

- [ ] Keep existing high line/branch coverage while reducing tests coupled only to private internals when behavior coverage exists.
- [ ] Add mutation testing for bounds, constraints, nullability, locale fallback, escaping, and seed propagation.
- [ ] Enable strict compiler warnings and select one focused static analyzer.
- [ ] Test Java 21 plus the current supported LTS/latest JDK.
- [ ] Add compatibility, JPMS, Kotlin immutable, Kotest replay, Spring slice, and Central-artifact checks to CI.
- [ ] Set mutation and analysis thresholds from a measured baseline; do not introduce meaningless 100% targets.

**Tests**

- [ ] Seed, bounds, nullability, and constraint mutants are killed.
- [ ] CI fails on a deliberate binary break and a deliberate docs fact mismatch.
- [ ] All supported JDK jobs run the same contract suite.

**Done when:** The quality system detects broken behavior, not merely unexecuted lines.

### Step 3.10 — Establish honest performance budgets

**Depends on:** 2.10, stable Stage 3 APIs.

**Actions**

- [ ] Separate scalar, object, semantic-object, schema, and bulk export benchmarks.
- [ ] Make competitor workloads equivalent or label the semantic work difference explicitly.
- [ ] Use at least three forks and publish confidence intervals.
- [ ] Define regression budgets for representative workloads, memory, and allocation.
- [ ] Profile before optimizing object generation.
- [ ] Restore an automated benchmark cadence or remove the monthly claim.
- [ ] Present the complete dashboard rather than cherry-picked scalar wins.

**Tests**

- [ ] Quick benchmarks detect gross regressions in pull requests.
- [ ] Full benchmarks are reproducible from one documented command/environment.
- [ ] Marketing claims are mechanically linked to current results.

**Done when:** Performance work and claims are based on comparable, reproducible evidence.

### Step 3.11 — Make releases immutable, resumable, and verifiable

**Depends on:** 1.7, stable Stage 3 artifacts.

**Actions**

- [ ] Use a reviewed release PR to freeze version, changelog, docs, API baseline, and artifact metadata.
- [ ] Trigger publication from an immutable signed tag.
- [ ] Separate build/sign, upload, Central validation/release, GitHub release, and post-publication smoke checks into idempotent stages.
- [ ] Record Central deployment identifiers so a partial run can resume.
- [ ] Prevent reruns from attempting to republish an existing immutable version.
- [ ] Verify signatures, checksums, BOM, SBOM, provenance, sources, Javadocs, module metadata, and POMs.
- [ ] Add a recovery runbook for failure at every stage.
- [ ] Rehearse the process with a non-GA prerelease.

**Tests**

- [ ] Simulate failure after Central upload and resume without duplicate publication.
- [ ] Simulate GitHub release failure after Central success and recover safely.
- [ ] Clean Maven/Gradle smoke projects resolve from Central only.

**Done when:** A release either completes verifiably or resumes from known state without changing its artifacts.

### Step 3.12 — Complete v2 documentation and migration material

**Depends on:** 3.1–3.11.

**Actions**

- [ ] Write the 1.6-to-v2 migration guide by use case, not by internal class.
- [ ] Publish random-source, recipe, construction, error, safety, provider, locale, schema, and thread-use contracts.
- [ ] Add known limitations for unsupported reflection, Kotlin, JPMS, schemas, shrinking, and production data use.
- [ ] Update every quick-start and integration example to v2 canonical APIs.
- [ ] Generate a compatibility matrix for Java/JDK, Kotlin, Spring Boot, Kotest, jqwik, Jackson, Maven, Gradle, sbt, and Mill.
- [ ] Add a release/adoption checklist for consumer projects.

**Tests**

- [ ] All snippets compile against locally published v2 artifacts.
- [ ] Documentation facts and links pass.
- [ ] A maintainer unfamiliar with an implementation package can follow the release runbook in a dry-run.

**Done when:** The supported behavior and migration path can be understood without reading source code.

---

## Stage 4: Prove v2 Through Alphas, Release Candidates, and Real Projects

**Goal**: Validate the frozen contract outside the repository before declaring GA.
**Success Criteria**: Two consecutive release candidates pass the complete artifact matrix and two representative consumer projects, with no unresolved P0 regression and a rehearsed rollback.
**Tests**: Central prerelease resolution, clean consumer builds, replay drills, fixture validation, performance comparison, migration rehearsals, rollback, and pilot-project full test suites.
**Status**: Not Started

### Step 4.1 — Publish v2 alpha for contract feedback

**Depends on:** Stage 3.

**Actions**

- [ ] Freeze the proposed v2 API and recipe format for alpha review.
- [ ] Publish alpha artifacts through the new release process.
- [ ] Run every clean consumer example from the remote repository.
- [ ] Invite focused feedback on migration, diagnostics, construction, replay, and adapter behavior.
- [ ] Accept alpha changes only when they simplify or correct the contract.

**Tests**

- [ ] All artifact/signature/provenance checks pass remotely.
- [ ] The 1.6 migration guide produces compiling alpha consumers.
- [ ] Golden recipes replay after download from Central.

**Done when:** The contract is externally consumable and no known P0 design ambiguity remains.

### Step 4.2 — Select and baseline two pilot projects

**Depends on:** 4.1.

**Actions**

- [ ] Select one plain-Java library/service and one Kotlin/Spring application.
- [ ] Prefer projects with different build tools and meaningful fixture usage.
- [ ] Inventory existing faker/random utilities, property testing, snapshots, persistence, locales, and sensitive-looking fixtures.
- [ ] Record baseline test duration, flaky failures, fixture validity, dependency graph, and replay capability.
- [ ] Define a project-owned fixture adapter and dependency-level rollback.
- [ ] Choose one bounded fixture domain per pilot; do not migrate everything.

**Tests**

- [ ] Both projects are green before migration.
- [ ] Each has a documented rollback that requires no application-code rewrite.
- [ ] Each can demonstrate its current failure-replay method or explicitly records the absence.

**Done when:** The pilots are representative, measurable, bounded, and reversible.

### Step 4.3 — Migrate the plain-Java pilot

**Depends on:** 4.2.

**Actions**

- [ ] Import the v2 BOM and only required modules.
- [ ] Implement a small project-owned fixture interface backed by kRandom.
- [ ] Pin locale, clock, safety policy, construction policy, and recipe conventions.
- [ ] Migrate one fixture domain while keeping the old implementation available.
- [ ] Compare generated validity, test runtime, diagnostics, and dependency footprint.
- [ ] Trigger and replay a deliberate seeded failure.

**Tests**

- [ ] The complete project test suite passes repeatedly.
- [ ] No generated fixture escapes into production/external systems.
- [ ] The deliberate failure replays locally and in CI.
- [ ] Switching the adapter back restores the prior behavior.

**Done when:** The Java pilot proves adoption without library leakage or irreversible coupling.

### Step 4.4 — Migrate the Kotlin/Spring pilot

**Depends on:** 4.2, preferably after lessons from 4.3.

**Actions**

- [ ] Import the BOM, Kotlin DSL/Kotest module, and Spring integration as required.
- [ ] Exercise immutable Kotlin data classes and the real `@KrandomTest` slice.
- [ ] Replay a property-test failure through the host framework seed.
- [ ] Validate Bean Validation fixtures through the application's configured validator.
- [ ] Compare context startup, test duration, diagnostics, and dependency footprint.
- [ ] Keep the migration behind a project-owned fixture interface.

**Tests**

- [ ] The complete project test suite passes repeatedly.
- [ ] Kotlin non-null properties never receive runtime nulls.
- [ ] Kotest replay and Spring property configuration work in CI.
- [ ] The adapter rollback restores the prior fixture implementation.

**Done when:** The most demanding supported integration stack works in a real application.

### Step 4.5 — Triage pilot feedback and cut RC1

**Depends on:** 4.3, 4.4.

**Actions**

- [ ] Classify pilot findings as P0 contract defects, P1 release-quality defects, migrations, documentation, or deferred enhancements.
- [ ] Fix every P0/P1 item with focused tests.
- [ ] Reject feature requests that expand v2 scope without a contract need.
- [ ] Update compatibility, performance, and migration evidence.
- [ ] Freeze public APIs and recipe format at RC1.
- [ ] Publish RC1 and rerun both pilots from remote artifacts.

**Tests**

- [ ] RC1 passes all repository and pilot suites.
- [ ] Compatibility comparison against the frozen alpha has only reviewed changes.
- [ ] No undocumented migration step remains.

**Done when:** RC1 represents the intended GA contract.

### Step 4.6 — Run stability soak and release-recovery rehearsal

**Depends on:** 4.5.

**Actions**

- [ ] Run repeated deterministic, concurrent, schema export, and pilot suites across supported JDKs.
- [ ] Exercise large object graphs and bulk exports under performance budgets.
- [ ] Rehearse release failure/resume and consumer rollback.
- [ ] Monitor dependency/security advisories for release inputs.
- [ ] Make only blocker fixes after the freeze.

**Tests**

- [ ] Repeated runs have no unexplained flake or recipe divergence.
- [ ] No benchmark exceeds its agreed regression budget without an approved explanation.
- [ ] Release and pilot rollback rehearsals succeed.

**Done when:** The frozen contract survives sustained use and operational failure drills.

### Step 4.7 — Cut RC2 and make the GA decision

**Depends on:** 4.6.

**Actions**

- [ ] Publish RC2 with blocker fixes only.
- [ ] Run the full repository, artifact, and pilot matrix again.
- [ ] Audit every v2 GA checklist item and unresolved issue.
- [ ] Require an explicit no-go if any P0 remains or either pilot cannot replay/rollback.
- [ ] Record the GA decision and accepted residual risks.

**Tests**

- [ ] RC2 passes two pilots and all clean consumers.
- [ ] RC1-to-RC2 compatibility changes are blocker-only and documented.
- [ ] No P0 issue remains open.

**Done when:** Two consecutive RCs establish release confidence and the GA decision is evidence-backed.

---

## Stage 5: Release v2 GA and Adopt It Safely

**Goal**: Publish the proven contract, migrate projects in controlled waves, and establish long-term compatibility and support discipline.
**Success Criteria**: GA artifacts pass remote verification; adoption waves complete without systemic regressions; every migrated project can replay failures and roll back; post-GA policy protects the v2 contract.
**Tests**: Final release matrix, Maven Central smoke tests, canary monitoring, per-project suites, replay drills, rollback checks, compatibility gates, and post-release incident review.
**Status**: Not Started

### Step 5.1 — Publish and verify v2 GA

**Depends on:** Stage 4 GA approval.

**Actions**

- [ ] Complete the release PR with final version, changelog, migration guide, API baseline, compatibility matrix, and support policy.
- [ ] Create the immutable signed GA tag.
- [ ] Run the resumable Central publication flow.
- [ ] Verify every artifact, BOM, signature, checksum, SBOM, provenance, source, Javadoc, POM, and module metadata.
- [ ] Run clean Maven and Gradle smoke tests from Maven Central.
- [ ] Create the GitHub release only after remote artifacts are confirmed.
- [ ] Announce known limitations, replay recipe, migration path, and support channels.

**Tests**

- [ ] Remote artifacts are byte-identical to approved release outputs where the process promises reproducibility.
- [ ] All canonical quick starts work from Central.
- [ ] The release workflow records a complete, auditable state.

**Done when:** v2 GA is remotely usable, verified, documented, and recoverable.

### Step 5.2 — Inventory and classify all adoption candidates

**Depends on:** 5.1.

**Actions**

- [ ] Search each project for hand-written fixture factories, random utilities, faker libraries, property adapters, snapshot data, and generated persisted data.
- [ ] Record language, framework, build tool, JDK, JPMS, test scale, locale needs, sensitive-data use, and current replay mechanism.
- [ ] Rank projects by adoption value and migration risk.
- [ ] Exclude production data-generation paths until separately reviewed.
- [ ] Define project-specific success metrics and rollback before scheduling migration.

**Tests**

- [ ] Every candidate has an owner, risk class, bounded first domain, and rollback.
- [ ] No project is scheduled only because it is easy; the waves include representative risk.

**Done when:** Adoption order is based on evidence rather than repository order or enthusiasm.

### Step 5.3 — Run canary and expansion waves

**Depends on:** 5.2.

**Actions**

- [ ] Wave 0: retain the two RC pilots as GA canaries.
- [ ] Wave 1: migrate low-risk Java test-only projects.
- [ ] Wave 2: migrate Kotlin/property-testing projects.
- [ ] Wave 3: migrate Spring/JPMS/large-fixture projects.
- [ ] Migrate one bounded fixture domain per project change.
- [ ] Keep a project-owned fixture adapter and the previous implementation for one project release.
- [ ] Pin BOM and approved generation profiles.
- [ ] Stop the wave if a shared contract regression appears.

**Tests**

- [ ] Every project passes its full suite repeatedly.
- [ ] Every project replays a deliberate seeded failure.
- [ ] Every project performs or mechanically verifies rollback.
- [ ] Test time, flake rate, validity failures, and dependency changes stay inside project budgets.

**Done when:** All approved projects adopt v2 without losing reversibility or replayability.

### Step 5.4 — Publish organization-wide fixture standards

**Depends on:** Lessons from 5.3.

**Actions**

- [ ] Publish approved profiles for locale, fixed clock, recipe persistence, safety policy, construction policy, and error policy.
- [ ] Define when random fixtures are appropriate versus builders, golden fixtures, or production-like datasets.
- [ ] Forbid kRandom use for production identities, payments, account creation, or unreviewed external traffic.
- [ ] Standardize CI replay output and failure retention.
- [ ] Provide Java, Kotlin, Spring, JUnit, jqwik, Kotest, Maven, and Gradle templates.
- [ ] Define upgrade cadence and compatibility review ownership.

**Tests**

- [ ] Templates compile against the GA BOM.
- [ ] Standards examples replay from recorded recipes.
- [ ] Security/safety examples use only approved test-safe modes.

**Done when:** New projects can adopt the same safe conventions without rediscovering them.

### Step 5.5 — Run post-GA stabilization and close the program

**Depends on:** 5.1–5.4.

**Actions**

- [ ] Track GA defects, migration friction, recipe divergence, performance regressions, and documentation gaps.
- [ ] Ship patch releases only when golden-stream and compatibility gates pass.
- [ ] Review adoption metrics after each wave.
- [ ] Move deferred enhancements into a post-v2 roadmap only after the contract is stable.
- [ ] Record lessons from the release and adoption program.
- [ ] Mark this plan complete only when GA and the agreed adoption scope are complete.

**Tests**

- [ ] Patch release rehearsal proves compatibility and seed promises.
- [ ] No unresolved cross-project P0/P1 regression remains.
- [ ] Deferred feature work cannot bypass normal prioritization.

**Done when:** v2 operates as a maintained platform rather than a one-time release project.

---

## 8. Traceability matrix

| Audit finding | Primary work packages | Release gate |
|---|---|---|
| Invalid/partial object fixtures | 2.1–2.5 | Stage 2 |
| Nested generic loss | 2.2 | Stage 2 |
| Bean Validation gaps | 2.3 | Stage 2 |
| Ambiguous random ownership/reseed | 2.6, 2.7, 3.2 | Stage 2/3 |
| Public API breadth/aliases | 1.2–1.4, 3.1 | Stage 1/3 |
| Kotlin immutable types/DSL | 2.5, 3.4 | Stage 3 |
| Kotest replay/shrinking | 3.3 | Stage 3 |
| Spring slice/default mismatch | 3.5 | Stage 3 |
| JUnit/jqwik replay | 3.6 | Stage 3 |
| Registry/provider duplication | 2.8 | Stage 2 |
| Financial/identity safety | 2.9 | Stage 2 |
| Core Kotlin dependency/BOM | 1.6 | Stage 1 |
| JPMS/reflection behavior | 2.4, 3.8 | Stage 3 |
| Schema scope/silent metadata errors | 3.7 | Stage 3 |
| Coverage without contract tests | 3.9 | Stage 3 |
| Misleading benchmark claims | 3.10 | Stage 3 |
| Documentation drift | 1.5, 3.12 | Stage 1/3 |
| Supply-chain gaps | 1.7 | Stage 1 |
| Fragile release workflow | 3.11 | Stage 3 |
| Organization-wide rollout risk | 4.2–5.5 | Stage 4/5 |

## 9. Metrics

Track these at every stage gate:

- Open P0/P1 defects by contract area.
- Public APIs kept, deprecated, and removed.
- Binary/source compatibility violations.
- Supported constraint/type cases and mutation score for critical contracts.
- Golden recipe replay pass rate across modules and JDKs.
- Clean consumer matrix pass rate.
- Documentation fact/snippet/link failures.
- Release inputs pinned/verified and artifact verification pass rate.
- Benchmark throughput, allocation, memory, and regression-budget breaches by workload.
- Pilot/adoption project test time, flake rate, fixture-validation failures, replay success, and rollback success.

Coverage remains a quality signal, but it is not the primary v2 success metric. The primary metric is whether advertised contracts survive independent consumer use.

## 10. Program completion checklist

- [ ] All D1–D10 decisions are recorded.
- [ ] The 1.6 bridge is released and verified.
- [ ] Every Stage 2 correctness gate passes.
- [ ] Every integration honors its host framework's replay/configuration model.
- [ ] The v2 API and recipe format are frozen and compatibility-protected.
- [ ] JPMS and all published-artifact consumers pass.
- [ ] Documentation facts and snippets are checked automatically.
- [ ] Build inputs and release artifacts are verified; release recovery is rehearsed.
- [ ] Performance claims and budgets use representative evidence.
- [ ] Two consecutive RCs pass two real-project pilots.
- [ ] v2 GA resolves and runs from Maven Central.
- [ ] Adoption waves complete with replay and rollback in every migrated project.
- [ ] Organization fixture standards and post-GA ownership are published.

Only after every checked item is true should this plan be marked **Complete**.
