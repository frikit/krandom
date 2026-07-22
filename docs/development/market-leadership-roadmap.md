# kRandom Market Leadership Roadmap

**Created:** 2026-07-22

**Status:** Proposed

**Starting point:** `2.1.0-SNAPSHOT` with the local v2.1 contract and release safeguards complete

**Purpose:** Make kRandom the most trustworthy and productive JVM fixture-generation framework,
not merely the framework with the largest provider catalog.

This roadmap starts after the reviewed v2.1 release gate. It does not authorize a commit, tag,
publication, or change in the v2.1 release scope.

## Product thesis

kRandom should win on a combination competitors do not currently offer in one coherent contract:

1. **Replayable by default** — every failure carries enough value-free information to reproduce it.
2. **Semantically valid** — generated objects and related fields agree, constraints are honored, and
   unsupported states fail contextually.
3. **Test-safe** — identities, payments, banking values, network values, and contributed data have
   explicit safety and provenance contracts.
4. **Fast with honest evidence** — equivalent workloads, allocation budgets, and reproducible
   reports support every performance claim.
5. **Controllable without cleverness** — Java and Kotlin users can target, compose, derive, and
   explain fixtures without stringly typed surprises.
6. **Small at the center, extensible at the edge** — core remains focused while local data packs and
   integration modules can grow independently.

Provider count is not the north star. A provider that has no strong use case, provenance, validity
contract, or maintainer is a liability rather than progress.

## What “number one” means

kRandom may call itself the leading JVM fixture framework only when the evidence below is public and
repeatable. Until then, documentation should state the specific measured strength instead of using a
general “number one” claim.

| Dimension | Leadership gate |
|:---|:---|
| Fixture control | The executable comparison corpus covers the 40 most common scalar, object, nested, collection, schema, and test-integration tasks with no unresolved P0 gap. |
| Replay and diagnosis | Every supported integration reports the effective seed and portable recipe; every object failure identifies the type and property path; one documented action replays a captured failure. |
| Correctness | Core retains the existing 100% rounded line and 99.9% aggregate branch gates; high-risk contract packages meet a measured mutation threshold; million-sample invariant suites find no unexplained validity failure. |
| Performance | On semantically equivalent workloads kRandom is either fastest or within 10% of the fastest maintained JVM competitor, with no representative workload regressing more than its approved budget. |
| Compatibility | Clean Java, Kotlin, Spring, JUnit, Kotest, JPMS, Maven, Gradle, sbt, Mill, and native-image consumers pass the published compatibility matrix. |
| Adoption | At least five independent external projects use released artifacts, including Java and Kotlin/Spring consumers, and can demonstrate replay and dependency-level rollback. |
| Market evidence | Maven Central downloads and public dependent repositories are baselined each release; a broad leadership claim requires first place on at least one adoption measure and top-three placement on the other. |
| Maintenance | Supported releases have a published compatibility window, security policy, release cadence, and issue-triage expectations that the maintainer can actually sustain. |

The adoption measures are deliberately independent of GitHub stars. Stars indicate attention, not
successful fixture generation.

## Competitive baseline

This baseline is a requirements input, not a promise to copy every competitor feature.

| Framework | Current strength to respect | kRandom response |
|:---|:---|:---|
| DataFaker | 263-provider catalog, expression syntax, and multi-format schema transformers | Keep migration compatibility for common providers and expressions; compete on validity, safety, replay, performance, and curated extensibility rather than novelty-provider count. |
| Instancio | Typed and scoped selectors, strict unused-selector detection, derived models, assignment rules, and deep JUnit integration | Close the fixture-control and test-injection gaps while preserving semantic defaults and portable recipes. |
| Fixture Monkey | Nested path expressions, Java/Kotlin object control, plugins, and IDE assistance | Provide type-safe Java/Kotlin targeting and a stable extension contract before considering IDE tooling. |
| Easy Random | Very small object-generation entry point and a familiar extension model | Preserve one-call generation and migration paths, but offer a stricter contract, modern integrations, and active release evidence. |

Research sources, checked on 2026-07-22:

- [DataFaker providers](https://www.datafaker.net/documentation/providers/)
- [DataFaker schemas and transformers](https://www.datafaker.net/documentation/schemas/)
- [Instancio user guide](https://www.instancio.org/user-guide/)
- [Fixture Monkey overview](https://naver.github.io/fixture-monkey/docs/introduction/overview/)
- [Easy Random project status](https://github.com/j-easy/easy-random#project-status)

## Stage 1: Ship v2.1 and establish the evidence baseline

**Goal**: Finish the external v2.1 gate, replace subjective parity claims with an executable product
scorecard, and identify the first real adopters.

**Success Criteria**:

- v2.1 artifacts, BOM, signatures, checksums, SBOMs, provenance, sources, Javadocs, POMs, and JPMS
  metadata are verified from Maven Central.
- The repository contains a versioned comparison corpus of at least 40 real tasks, grouped into
  scalar data, semantic data, object construction, fixture customization, constraints, replay,
  schemas, integrations, and extension use cases.
- Every comparison records `supported`, `partial`, `unsupported`, or `not applicable`, with an
  executable kRandom example and a source link for the competitor behavior.
- Benchmark workloads distinguish raw scalar speed, arbitrary object population, semantically
  populated objects, schema generation, and bulk export.
- The benchmark protocol uses equivalent semantics, at least three forks, confidence intervals,
  allocation measurements, pinned environments, and raw result retention.
- Maven Central downloads, public dependents, documentation traffic if available without user
  tracking, issue volume, and migration completion time have written baselines.
- One plain-Java and one Kotlin/Spring pilot are selected with owners, bounded fixture domains,
  before-state measurements, and rollback plans.
- README claims link to generated evidence; no stale “monthly” or leadership claim survives without
  an automated cadence.

**Tests**:

- `./scripts/pre_commit_check.sh`, `./scripts/verify_examples_local.sh`, release rehearsal, and
  Central-only consumer verification.
- A comparison-corpus verifier fails on a missing source, missing executable example, or unclassified
  scenario.
- Quick benchmark smoke checks detect deliberately injected gross regressions.
- A clean environment reproduces the published benchmark report from one documented command.

**Status**: Not Started

## Stage 2: Deliver best-in-class fixture control

**Goal**: Make complex object fixtures as precise and maintainable as hand-written builders while
retaining kRandom's semantic defaults.

**Success Criteria**:

- Java users can target properties with refactor-safe method references where the language and
  object model permit it; Kotlin users can target properties through the typed DSL.
- Nested paths, type selectors, predicates, collection elements, and explicit scopes have one
  documented matching and precedence model.
- Strict mode reports every unused, ambiguous, or shadowed rule by default; leniency is explicit at
  the narrowest useful scope.
- Rules can express set, generate, ignore, include, nullability, collection size, subtype, and
  dependent/correlated value behavior without separate mental models.
- Reusable fixture models are immutable values that support composition, derivation, nesting, and
  deliberate override precedence; applying a model cannot silently mutate another test.
- Generic types, records, sealed hierarchies, interfaces with configured subtypes, nested
  collections/maps, Kotlin data/value classes, and constructor/factory selection follow the same
  recursive type contract.
- Existing string paths remain supported through the documented compatibility window and fail with
  a suggested typed replacement where one is available.
- The common one-call APIs remain one-call APIs; advanced control does not make scalar or default
  object generation harder.

**Tests**:

- Compile-time Java method-reference and Kotlin property-reference examples.
- Golden selector tests for nested, scoped, inherited, collection, map, generic, ambiguous, unused,
  and lenient cases.
- Model tests for immutable reuse, derivation, nested application, override precedence, concurrency,
  and deterministic replay.
- Cross-language parity fixtures prove Java and Kotlin rules produce the same effective model.
- Migration tests compile representative Instancio and Fixture Monkey scenarios after translation.
- Full mutation coverage for rule matching, precedence, strictness, recursive types, and model
  composition.

**Status**: Not Started

## Stage 3: Make replay and explainability the category-defining feature

**Goal**: Turn every generated value and every failed fixture into an inspectable, portable, and
safe diagnostic experience.

**Success Criteria**:

- A value-sanitized generation report can expose the effective recipe, seed source, algorithm,
  locale, clock, profile, safety policies, construction path, matched rules, applied constraints,
  provider fallback, and failure path without exposing generated or user-supplied values.
- An `explain` mode answers why a property received its generator and why a higher-priority rule did
  or did not match.
- Standalone, JUnit, Kotest, Kotlin DSL, Spring, object, and schema entry points share the same recipe
  and diagnostic vocabulary.
- JUnit supports opt-in generated field/parameter injection and parameterized sources while keeping
  one seed lifecycle per test and reporting the complete replay recipe on failure.
- CI can persist a small replay artifact and rerun it locally with one documented API or command;
  the artifact is versioned, bounded, and contains no generated values or secrets.
- Parallel generation derives deterministic child streams independent of task scheduling; callers
  can choose ordered replay or explicitly non-stable high-throughput mode.
- Recipe-format and algorithm migrations have golden compatibility fixtures and an explicit
  upgrade/expiry policy.
- Diagnostics add negligible cost when disabled and have measured, budgeted overhead when enabled.

**Tests**:

- Value-leak tests with canary secrets in custom suppliers, exceptions, rule names, and diagnostics.
- Golden recipe replay across supported JDKs, Java/Kotlin entry points, JUnit/Kotest, Spring, JPMS,
  and native-image smoke consumers.
- Deterministic parallel tests under reordered executors and repeated CI runs.
- Mutation tests for seed precedence, child-stream derivation, rule explanations, redaction, and
  failure-path construction.
- Failure-injection tests verify the exact local replay procedure from a persisted CI artifact.
- JMH budgets cover disabled diagnostics, enabled reports, and replay parsing.

**Status**: Not Started

## Stage 4: Build a safe extension and data ecosystem

**Goal**: Let users add domain data and type support without bloating core, weakening determinism,
or accepting unknown provenance.

**Success Criteria**:

- The current University-only local pack becomes a versioned, domain-neutral pack contract with
  declared schema, locale, source, license, checksum, size bounds, and compatibility version.
- Local CSV, JSON, and JSONL feeds can map rows into schemas or typed fixtures with deterministic
  selection, bounded memory, contextual row diagnostics, and no runtime network access.
- A documented, configuration-scoped provider/type extension contract supports discovery without
  global mutable registries and without making extension implementation classes part of core's
  permanent public API.
- An offline pack linter validates manifest shape, license metadata, checksums, encoding, duplicate
  keys, locale/script sanity, field invariants, and sample generation before a contribution is
  reviewed.
- Extension compatibility is checked against the oldest supported minor release and the current
  development line.
- New provider families require a real consumer use case, stable data source, owner, safety class,
  validity tests, and maintenance plan; novelty catalogs do not enter core.
- Native locale growth prioritizes current fallback variants and contributor demand, and each new
  locale passes the existing provenance and quality gates.
- At least two independently maintained extension/data packs prove the contract before a registry or
  discovery website is considered.
- Protobuf, Vavr, Guava, persistence/JPA, and other integration modules are selected by pilot demand,
  not added speculatively; each module must have two consumers or one strategic pilot.

**Tests**:

- Pack/feed fixtures cover valid input, malformed rows, duplicate keys, checksum mismatch, size
  limits, encoding errors, unknown versions, unsafe paths, and deterministic replay.
- Extension compatibility test kits run against the support window and reject accidental SPI
  breaks.
- Isolation and concurrency tests prove extensions cannot leak between configurations or tests.
- Dependency and JPMS checks prove core remains focused and consumers import only the modules they
  request.
- Native-image smoke tests include one external extension and one local data pack.
- License/provenance verification is mandatory for every bundled or published dataset.

**Status**: Not Started

## Stage 5: Prove leadership through adoption and sustained quality

**Goal**: Convert technical differentiation into real migrations, a healthy contributor path, and
public evidence that survives more than one release.

**Success Criteria**:

- Five independent pilot projects complete bounded migrations: at least two plain Java, two
  Kotlin/Spring or Kotlin/property-testing, and one JPMS, native-image, or large-schema consumer.
- Every pilot records before/after fixture code size, test duration, allocation where relevant,
  flake rate, validity defects, diagnosis time, dependency footprint, and rollback outcome.
- Migration guides cover the top comparison-corpus scenarios from DataFaker, Instancio, Fixture
  Monkey, Easy Random, and hand-written Object Mother/builders.
- Copyable starter projects cover Java/Maven, Java/Gradle, Kotlin/Gradle, Kotlin/Spring, JUnit,
  Kotest, schema export, JPMS, and native-image use.
- Documentation has task-first navigation, an API chooser, searchable generator/extension catalog,
  honest limitations, runnable examples, and one canonical path per common task.
- Release cadence, compatibility window, deprecation policy, vulnerability handling, data-license
  policy, and issue-triage expectations are published and exercised for two consecutive releases.
- Contributor automation can create and validate a provider, locale, integration, or data pack
  without knowledge of core internals.
- Performance dashboards and adoption measures update automatically; regressions and stale claims
  fail a repository check.
- The leadership scorecard passes for two consecutive releases before broad “number one” language is
  used.

**Tests**:

- Each pilot runs repeated full suites, replays a deliberate failure, and performs or mechanically
  verifies rollback.
- Fresh users follow each starter and migration guide in clean CI without repository-local artifacts.
- Documentation facts, snippets, links, compatibility matrices, benchmarks, and adoption snapshots
  are generated or mechanically checked.
- Release recovery and patch-compatibility rehearsals pass twice after the first leadership
  candidate release.
- A quarterly scorecard review records misses, owners, next actions, and any claim that must be
  narrowed or removed.

**Status**: Not Started

## Ordered execution slices

Each slice should be one small, independently reviewable change that compiles, passes focused tests,
updates this roadmap, and ends with a proposed Conventional Commit message.

1. Complete the v2.1 external release gate and move the API compatibility baseline.
2. Add the machine-readable leadership scorecard and claim verifier.
3. Add the 40-scenario comparison corpus with current kRandom results only.
4. Correct the benchmark protocol and generate the first complete baseline.
5. Select and baseline the first two external pilots.
6. Specify selector matching, precedence, strictness, and compatibility behavior.
7. Add Java typed selectors, then Kotlin typed selectors, behind the same behavior tests.
8. Add scopes and strict unused/ambiguous rule diagnostics.
9. Specify immutable fixture models and implement composition/derivation incrementally.
10. Add dependent assignments and collection/subtype controls only after precedence is stable.
11. Specify the redacted generation-report schema and prove its leak resistance.
12. Add explainability to standalone object generation, then propagate it to integrations.
13. Add JUnit injection/parameter sources and CI replay artifacts.
14. Define deterministic parallel-generation behavior and benchmark its cost.
15. Generalize the local data-pack contract without changing existing University behavior.
16. Add offline feed readers one format at a time: CSV, JSONL, then JSON.
17. Publish the extension compatibility test kit and prove it with two external examples.
18. Run three additional pilots, then close only the gaps demonstrated by their evidence.
19. Automate the performance, compatibility, documentation, and adoption scorecards.
20. Review two consecutive releases and decide whether the leadership claim is earned.

## Priority and release policy

| Priority | Work |
|:---|:---|
| P0 | v2.1 external validation, evidence baseline, typed/scoped strict rules, immutable models, portable diagnostics, replay compatibility, and real pilots |
| P1 | JUnit injection, deterministic parallel generation, general local feeds/packs, extension test kit, migration starters, and automated dashboards |
| P2 | Additional integration modules, IDE support, public pack registry, and provider/locale expansion backed by demonstrated demand |

Minor releases should deliver one coherent stage or a narrow vertical slice. A 3.0 release is
justified only by a measured contract improvement that cannot be delivered compatibly; the roadmap
must not manufacture breaking changes to create a marketing milestone.

## Non-goals and stop rules

- Do not chase DataFaker's provider total or import entertainment catalogs into core.
- Do not add runtime network loading, hidden telemetry, global mutable registries, or production-data
  generation paths.
- Do not add a module, abstraction, or SPI without two consumer examples or one strategic pilot.
- Do not optimize a benchmark until its semantic equivalence and allocation profile are reviewed.
- Do not accept a dataset without source, license, checksum, safety classification, and maintainer.
- Do not advertise exact seeded values as stable across an undocumented algorithm or recipe change.
- Do not weaken strictness, mutation quality, compatibility, or coverage gates to increase velocity.
- Do not call kRandom “number one” based on one microbenchmark, provider count, or GitHub stars.

## Completion condition

This roadmap is complete when all five stages pass, the leadership scorecard passes for two
consecutive releases, five external pilots can replay and roll back, and every public superiority
claim is linked to current reproducible evidence. Anything less is progress, not proof of category
leadership.
