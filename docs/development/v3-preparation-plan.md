# kRandom 3.0.0 Preparation Plan

**Created:** 2026-08-27
**Released baseline:** `2.2.0` (`v2.2.0`)
**Development line:** `3.0.0-SNAPSHOT`

This plan starts the v3 development line without pretending that a major release is ready. Version
`3.0.0` reaches GA only after the project approves a concrete contract improvement, demonstrates
why it cannot be delivered compatibly, and publishes an executable migration path.

## Stage 1: Close the 2.2 release line

**Goal**: Start v3 from the exact released `2.2.0` API and artifact baseline.

**Success Criteria**: `latestGaVersion` remains `2.2.0`; `apiBaselineVersion` is `2.2.0`;
`developmentVersion` and all repository-local consumer defaults are `3.0.0-SNAPSHOT`; obsolete v2
API evolution classifications are cleared; the API-contract task compares v3 against the released
2.2 artifacts.

**Tests**: `./scripts/verify_documentation_facts.sh`, `./gradlew checkApiContract`, and searches for
stale active snapshot coordinates.

**Status**: Complete

## Stage 2: Replace documentation archaeology with a maintained map

**Goal**: Keep user, contributor, release, migration, benchmark, and current design documentation;
remove completed plans, stale reviews, and superseded competitive snapshots that remain available
in Git history and the `v2.2.0` tag.

**Success Criteria**: `docs/README.md` identifies every maintained documentation family; current
documents contain no links to deleted files; public examples distinguish the stable 2.2.0 release
from the v3 snapshot; internal Markdown links are mechanically checked.

**Tests**: Repository-wide Markdown link validation, docs-site link validation, documentation-fact
validation, and Markdown formatting.

**Status**: Complete

## Stage 3: Approve the v3 contract

**Goal**: Select the smallest user-visible contract that justifies a major release.

**Success Criteria**: The accepted proposal states the consumer problem, affected public symbols
or behavior, compatibility alternatives considered, migration steps, replay implications,
performance budget, and rollback path. If the work can be additive, it is scheduled for a 2.x
minor instead of manufacturing a breaking change.

**Tests**: API-diff rehearsal against 2.2.0, compile-tested before/after consumer examples, golden
replay fixtures where deterministic output is affected, and an approved migration-guide outline.

**Status**: Not Started

## Stage 4: Implement the approved contract incrementally

**Goal**: Deliver the v3 contract in small, independently verified slices.

**Success Criteria**: Each slice has behavior tests, Javadocs, changelog coverage, a narrow API
classification, and no unrelated abstraction. Exact 100% JaCoCo coverage and the measured
critical-path mutation thresholds remain enforced.

**Tests**: Focused module tests after every slice, followed by `./scripts/pre_commit_check.sh` and
`./scripts/verify_examples_local.sh` at each public contract boundary.

**Status**: Not Started

## Stage 5: Release and verify 3.0.0

**Goal**: Publish an independently usable major release with a tested migration and recovery path.

**Success Criteria**: Changelog and migration guide are final; release rehearsal passes; Central
artifacts, signatures, checksums, SBOMs, provenance, sources, Javadocs, POMs, BOM, and JPMS metadata
are verified; clean Java and Kotlin/Spring consumers resolve the public artifacts; the GitHub tag
and release point to the exact release commit.

**Tests**: `./scripts/verify_release_rehearsal.sh 3.0.0`, the complete pre-commit and local consumer
gates, Central-only consumer verification, artifact attestation verification, and post-release
documentation-fact validation.

**Status**: Not Started

## Working rules

- Keep `2.2.0` as the public installation version until 3.0.0 exists on Maven Central.
- Do not add an API compatibility exclusion without its migration example and changelog entry.
- Do not remove an API merely to justify the major number.
- Keep each implementation slice small enough to review, test, and reverse independently.
- Ask before every commit; commits use Conventional Commits and contain no assistant attribution.
