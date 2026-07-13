# v2 Release-Readiness Audit — 2026-07-09

## Stage 1: Repository Inventory
**Goal**: Establish the current modules, public artifacts, history, existing plans, and working-tree baseline.
**Success Criteria**: Current repository structure and prior review claims are catalogued without relying on stale documentation.
**Tests**: Inspect Git status/history, module builds, repository files, policy documents, and previous reviews/plans.
**Status**: Complete

## Stage 2: Product and API Contract
**Goal**: Reconstruct kRandom's purpose, design principles, public entry points, extension model, and compatibility promises.
**Success Criteria**: The intended v2 product boundary and consumer contract are stated explicitly and checked against the implementation.
**Tests**: Trace facade, configuration, generator, object, schema, provider, locale, and integration APIs from source and tests.
**Status**: Complete

## Stage 3: Engineering and Release Audit
**Goal**: Audit implementation quality, tests, dependencies, documentation, packaging, CI, benchmarks, security posture, and release mechanics.
**Success Criteria**: Concrete strengths, defects, inconsistencies, and release risks are supported by repository evidence.
**Tests**: Static searches, targeted source/test inspection, artifact/API analysis, and release-workflow review.
**Status**: Complete

## Stage 4: Verification
**Goal**: Validate the current branch with the repository's own quality gates and focused checks for identified risks.
**Success Criteria**: Verification results are recorded, with failures reduced to reproducible root causes.
**Tests**: `JAVA_HOME=<JDK 21+> ./scripts/pre_commit_check.sh`, consumer-example verification where practical, and targeted compatibility checks.
**Status**: Complete

## Stage 5: v2 and Adoption Recommendation
**Goal**: Produce a prioritized, staged backlog for v2 and a safe rollout plan for adopting kRandom across other projects.
**Success Criteria**: Each recommendation has rationale, scope, acceptance criteria, priority, and an explicit release/adoption gate.
**Tests**: Cross-check recommendations against SemVer policy, migration needs, existing project ideology, and verification evidence.
**Status**: Complete

The executable follow-up is [`v2-master-implementation-plan.md`](v2-master-implementation-plan.md).
