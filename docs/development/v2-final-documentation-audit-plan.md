# v2.0.0 Final Documentation Audit

## Stage 1: Inventory release-facing documentation
**Goal**: Identify every user-facing version reference, installation example, API example, compatibility statement, and release instruction that applies to v2.0.0.
**Success Criteria**: Public documentation and executable consumer examples are separated from historical review and implementation records, with all confirmed drift recorded.
**Tests**: Search release-facing Markdown, version facts, example builds, and current public API declarations.
**Status**: Complete

## Stage 2: Update v2.0.0 content and examples
**Goal**: Align installation coordinates, API names, migration guidance, compatibility details, safety-policy examples, and release instructions with v2.0.0.
**Success Criteria**: A user can install v2.0.0, choose the correct module and API, migrate from 1.x, and run examples without relying on removed or unsafe defaults.
**Tests**: Cross-check each changed example against source signatures and existing integration tests.
**Status**: Complete

## Stage 3: Strengthen documentation validation
**Goal**: Make stale release versions and removed v2 API examples fail automated documentation checks.
**Success Criteria**: Documentation validation distinguishes the release documentation version from the previous API baseline and detects known removed aliases in public docs.
**Tests**: Run documentation fact, link, and formatting checks with intentional target patterns reviewed in the script.
**Status**: Complete

## Stage 4: Verify the complete release surface
**Goal**: Validate documentation, consumer examples, compilation, tests, API compatibility, Javadoc, SBOMs, and coverage before committing.
**Success Criteria**: All repository quality gates and local consumer examples pass with no uncommitted generated files.
**Tests**: `./scripts/verify_examples_local.sh` and `./scripts/pre_commit_check.sh` under Java 21.
**Status**: Complete
