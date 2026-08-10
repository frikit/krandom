# 6.1.0 release readiness — 2026-08-10

## Stage 1: Validate repository inputs
**Goal**: Verify source formatting, documentation, dependency pinning, module boundaries, and the complete test suite.
**Success Criteria**: The pre-commit and module-boundary checks pass under Java 21 or newer.
**Tests**: `./scripts/pre_commit_check.sh`; `./scripts/verify_module_boundaries.sh`.
**Status**: Complete

## Stage 2: Validate release consumers and artifacts
**Goal**: Verify the 6.1.0 release candidate locally and rehearse release artifact checks without publication.
**Success Criteria**: Local consumer examples and the release rehearsal for `6.1.0` pass.
**Tests**: `KRANDOM_VERSION=6.1.0 ./scripts/verify_examples_local.sh`; `./scripts/verify_release_rehearsal.sh 6.1.0`.
**Status**: Complete

## Stage 3: Exercise supplemental release gates
**Goal**: Run the focused acceptance, integration, native-image, and benchmark checks that are available locally.
**Success Criteria**: All required checks pass; unavailable optional tooling is recorded as skipped.
**Tests**: `verify_foundation_acceptance.sh`; `verify_foundation_integration_gate.sh`; `verify_native_image.sh`; `run_benchmarks.sh --quick`.
**Status**: Not Started

## Stage 4: Commit readiness records
**Goal**: Review and commit the completed release-readiness documentation.
**Success Criteria**: A conventional, trailer-free commit contains only the readiness records.
**Tests**: `git diff --check`; `git status --short`.
**Status**: Not Started
