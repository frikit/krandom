# Release 2.5.0: deterministic environment behavior

Scope: publish the compatible determinism fixes completed after 2.4.0. This release makes UUIDv7
respect the configured clock, removes JVM-default locale leakage from machine-oriented output, and
repairs semantic schema `id` resolution. It preserves the public API and the established 2.x
contract.

## Stage 1: Reproduce environment-dependent behavior
**Goal**: Capture clock and locale leakage with focused regression tests.
**Success Criteria**: Tests fail on the previous behavior for UUIDv7 timestamps, Turkish case
mapping, locale-sensitive CSS decimals, and semantic schema IDs.
**Tests**: `UUIDGeneratorTest` and isolated `EnvironmentDeterminismTest` cases.
**Status**: Complete

## Stage 2: Apply compatible fixes
**Goal**: Route UUIDv7 through `GeneratorConfig` and make normalization and formatting
locale-explicit.
**Success Criteria**: The regressions pass without public API changes or ordinary seeded-output
drift.
**Tests**: Affected generator suites, API contract checks, exact coverage, and mutation testing.
**Status**: Complete

## Stage 3: Qualify the release candidate
**Goal**: Exercise the complete repository and supported local consumer matrix before publication.
**Success Criteria**: The required pre-commit gate, local examples, release rehearsal, and main CI
all pass for the exact release commit.
**Tests**: `scripts/pre_commit_check.sh`, `scripts/verify_examples_local.sh`,
`scripts/verify_release_rehearsal.sh 2.5.0`, and CI.
**Status**: Complete

## Stage 4: Publish and close out 2.5.0
**Goal**: Publish signed artifacts and make the release independently verifiable.
**Success Criteria**: Maven Central, GitHub release assets and attestations, Central-only consumers,
documentation deployment, and post-release version facts all succeed.
**Tests**: Release workflow, asset integrity and attestation verification,
`KRANDOM_VERSION=2.5.0 scripts/verify_examples_central.sh`, and post-release CI.
**Status**: Complete

## Publication evidence

- Exact release commit on `main`: `a887c3d42a225f60dca390e378c642244a33c01d`.
- [Final qualification CI](https://github.com/frikit/krandom/actions/runs/35997680255) passed
  Java 21/25, exact coverage, schema mutation, GraalVM native image, SBOM, and the full local
  consumer matrix.
- [Release workflow](https://github.com/frikit/krandom/actions/runs/35998281924) validated, signed,
  attested, and uploaded deployment `82cc3523-0b5d-4bda-90e2-92b4a6d4360c` with automatic Central
  publication.
- [v2.5.0](https://github.com/frikit/krandom/releases/tag/v2.5.0) is a non-prerelease GitHub release
  that points to the exact release commit and contains 33 assets.
- All JSON SBOM component versions, the 177-entry aggregation bundle's ZIP integrity, and its
  GitHub build-provenance attestation passed verification.
- The public Maven Central BOM resolved successfully, and fresh Java Maven, Kotlin Spring Maven,
  and Java Gradle consumers passed without Maven-local resolution.
- Post-release facts retain `2.5.0` as latest GA, advance the API baseline to `2.5.0`, and advance
  repository development and example defaults to `2.6.0-SNAPSHOT`.
