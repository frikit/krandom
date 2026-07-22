# v2.1 Contract And Release Plan

**State:** Local implementation complete through the pre-publication gate. This plan does not
authorize a release, tag, Central upload, or external-consumer run by itself.

The next release should favor contract confidence over more provider breadth. The post-tag work is
additive public API, so the recommended version is **2.1.0**, subject to owner confirmation in
Stage 1. A patch release would not correctly describe those additions under semantic versioning.

## Stage 1: Confirm the v2.1 release boundary

**Goal**: Turn the post-v2.0.0 change set into a deliberate release train with one version decision
and a frozen scope.

**Success Criteria**:

- The owner explicitly chooses `2.1.0` or records a different versioning rationale.
- `CHANGELOG.md`, `gradle.properties`, documentation facts, and release coordinates describe the
  same development and latest-GA versions.
- Every public addition since `v2.0.0` has a use case, Javadocs, tests, a public API inventory
  entry, and a narrow API-evolution classification.
- New provider families and broad vocabulary imports are deferred until after the release.

**Tests**: Compare `v2.0.0..main`, run `./gradlew checkApiContract`,
`./scripts/verify_documentation_facts.sh`, and `./scripts/verify_docs_site_links.sh`.

**Status**: Complete

## Stage 2: Close the remaining v2 API-surface decisions

**Goal**: Complete the unfinished API consistency work tracked by master-plan Step 3.1 before
adding further public surface.

**Success Criteria**:

- The `URL`/`URI`/`UUID` facade naming decision is recorded, implemented, and reflected in the
  migration guide and API inventory.
- Each facade operation has one documented canonical name and a consistent overload pattern.
- A facade split is either justified by measured discoverability evidence or explicitly deferred;
  no speculative namespace abstraction is introduced.
- The next released version becomes the new API-compatibility baseline in the follow-up commit.

**Tests**: Compile all migration examples, run API-evolution checks against `v2.0.0`, add
compile-time coverage for the chosen canonical names, and run `./scripts/pre_commit_check.sh`.

**Status**: Complete

## Stage 3: Define the supported schema contract

**Goal**: Complete master-plan Step 3.7 by making JSON Schema/OpenAPI import and export behavior
precise, rather than growing formats opportunistically.

**Success Criteria**:

- The supported subset explicitly states behavior for `$ref`, compositions, recursion, enums,
  patterns, bounds, formats, and unknown provider metadata.
- Unsupported constructs fail with contextual diagnostics; none silently become unconstrained
  schemas.
- Schema import, export, projection, streaming, escaping, reuse, and thread-use guarantees are
  documented in one public guide.
- Recursive type and provider-metadata handling share the agreed model where it reduces duplicate
  behavior without changing supported output.

**Tests**: Contract fixtures for each accepted and rejected construct, supported import/export
round trips, concurrent/reuse tests, and regression coverage for JSONL, CSV, XML, SQL, YAML, and
TOML escaping.

**Status**: Complete

## Stage 4: Upgrade confidence and release recovery evidence

**Goal**: Make the existing high coverage meaningful across supported environments and release
failure paths.

**Success Criteria**:

- CI runs the same core contract suite on Java 21 and the current supported JDK.
- A measured mutation-testing pilot covers bounds, nullability, seed propagation, locale fallback,
  and escaping; any threshold is based on that baseline rather than a nominal target.
- The GraalVM native-image smoke check runs on at least one GraalVM-capable CI runner, while
  remaining optional locally.
- A release rehearsal records how to resume after Central upload or GitHub-release failure without
  republishing immutable artifacts.

**Tests**: Deliberate mutation and binary-break checks, multi-JDK CI matrix, native-image smoke
job, simulated partial-release recovery, and clean Maven/Gradle artifact smoke projects.

**Status**: Complete

## Stage 5: Publish and validate v2.1 externally

**Goal**: Release the frozen v2.1 contract and validate it outside this repository before starting
another feature cycle.

**Success Criteria**:

- A reviewed release PR freezes the version, changelog, API baseline, compatibility matrix, and
  release notes.
- Maven Central artifacts, BOM, signatures, checksums, SBOMs, provenance, sources, Javadocs, POMs,
  and JPMS metadata are verified remotely.
- At least one plain-Java and one Kotlin/Spring consumer resolve the released artifacts and replay a
  documented seeded failure.
- Post-publication release facts move `developmentVersion` to the next snapshot and preserve the
  new latest-GA baseline.

**Tests**: The release runbook, Central-only Maven/Gradle smoke tests, integration-consumer suites,
artifact attestation verification, and `./scripts/pre_commit_check.sh`.

**Status**: In Progress

**External prerequisite**: Publishing, Central Portal validation, attestation verification, and
Central-only consumer execution require a reviewed commit, repository secrets, and remote artifact
availability. The repository now contains the rehearsal, recovery workflow, plain-Java/Kotlin-Spring
fixtures, and `verify_examples_central.sh` command needed to perform that gate without changing
source again.

## Scope boundaries

- Do not copy DataFaker's entertainment, biometric, medical, or live model-name catalogs merely to
increase provider count.
- Do not alter release coordinates, trigger publication, or create a tag until the Stage 1 owner
decision is recorded.
- Treat a new native locale as a separate provenance and maintenance decision, not a v2.1 default.
- Keep every implementation increment small, tested, documented, and API-classified before it is
committed.
