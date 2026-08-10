# Dependency update merge — 2026-08-10

## Stage 1: Confirm pull-request readiness
**Goal**: Verify that PRs #84–#89 have no outstanding review feedback.
**Success Criteria**: Each PR is open and has no unresolved review thread.
**Tests**: Inspect pull-request reviews and threads.
**Status**: Complete

## Stage 2: Merge dependency updates
**Goal**: Merge the validated Dependabot pull requests into `main`.
**Success Criteria**: All six pull requests are merged successfully.
**Tests**: Inspect each pull-request merge result.
**Status**: Complete

## Stage 3: Validate locally
**Goal**: Update the local `main` checkout and validate the merged dependency set.
**Success Criteria**: The checkout matches `origin/main` and the full pre-commit script passes under Java 21 or newer.
**Tests**: `JAVA_HOME=<JDK 21+> ./scripts/pre_commit_check.sh`.
**Status**: Complete
