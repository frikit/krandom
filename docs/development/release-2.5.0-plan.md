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
**Status**: In Progress
