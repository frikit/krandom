# v2 Release Documentation Verification

## Stage 1: Audit release-facing documentation
**Goal**: Check the README, public docs site, migration guidance, version policy, and release runbook against the current build and published-release facts.
**Success Criteria**: Release users can find accurate installation, migration, compatibility, and publication guidance without relying on internal plans.
**Tests**: Run documentation fact and docs-site link validation; inspect version facts in `gradle.properties` and the release workflow.
**Status**: Complete

## Stage 2: Correct confirmed documentation drift
**Goal**: Fix inaccurate integration/module statements, expose the v2 migration guide, and make the runbook's version-update procedure actionable.
**Success Criteria**: The public site describes published integration modules correctly, links to v2 migration guidance, and the runbook matches the concrete version snippets in the repository.
**Tests**: Review changed links and commands against the repository's source, build facts, and workflow.
**Status**: Complete

## Stage 3: Verify release documentation
**Goal**: Format and validate every changed document and record the pre-release outcome.
**Success Criteria**: Markdown, facts, and docs-site links pass; the release documentation is ready for the v2 cut.
**Tests**: `./scripts/pre_commit_check.sh`.
**Status**: Complete
