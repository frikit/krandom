# Release 2.4.0: fixture-contract hardening

Scope: complete the repository-review improvements without changing the default seeded-output
contract. This release improves internal maintainability, makes Java/Kotlin fixture capabilities
explicit, strengthens test evidence, expands representative native-image coverage, and enforces
reviewed dependency artifact checksums. It does not introduce a v3 API or publish a new framework
abstraction.

## Stage 1: Record the compatible contract
**Goal**: Reconcile active roadmap/review documentation with the released 2.3.0 behavior and state
the Java/Kotlin fixture capability and precedence boundaries.
**Success Criteria**: No active document presents already-released profile rollback, clock snapshots,
or opt-in independent streams as pending work; supported differences are discoverable.
**Tests**: Markdown formatting, repository and docs-site links, documentation-facts verification.
**Status**: Complete

## Stage 2: Isolate object-generation responsibilities
**Goal**: Extract private stream-planning and rule-resolution responsibilities from the field
resolver without changing public APIs or default generated values.
**Success Criteria**: Existing seeded fixtures, recipes, rule precedence, and extension behavior
remain unchanged; extracted classes have focused behavior tests.
**Tests**: Core object-generation tests, API contract, performance smoke, full coverage and mutation
gate.
**Status**: Not Started

## Stage 3: Strengthen quality evidence
**Goal**: Add mutation coverage for deterministic configuration/replay and resolver behavior, and
replace the scalar-only native-image smoke fixture with representative core use cases.
**Success Criteria**: Mutation targets cover the new risk boundaries; native image exercises records,
reflection, localized resources, and a custom provider module.
**Tests**: `:core:pitest`, GraalVM native-image smoke in CI, full pre-commit gate.
**Status**: Not Started

## Stage 4: Enforce dependency artifact integrity
**Goal**: Establish a reviewed Gradle SHA-256 verification baseline and make it part of normal
verification.
**Success Criteria**: `gradle/verification-metadata.xml` contains no broad trust exceptions and a
cold build, API checks, tests, SBOM generation, and benchmarks resolve under verification.
**Tests**: Gradle verification on the full local gate and release rehearsal.
**Status**: Not Started

## Stage 5: Qualify and publish 2.4.0
**Goal**: Cut the compatible release after all previous stages are committed to `main`.
**Success Criteria**: Full pre-commit, local consumer matrix, release rehearsal, Maven Central
publication, GitHub release, Central-only consumers, and post-release version facts all succeed.
**Tests**: `scripts/pre_commit_check.sh`, `scripts/verify_examples_local.sh`,
`scripts/verify_release_rehearsal.sh 2.4.0`, CI and release workflow.
**Status**: Not Started
