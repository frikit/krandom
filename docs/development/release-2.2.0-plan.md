# Release 2.2.0

Release scope: the reviewed dependency, build-action, consumer-example, and documentation
maintenance changes. No v3 API redesign or behavioral fixes from the v3 review are included.

## Stage 1: Prepare release inputs
**Goal**: Bring maintenance and release facts together on a branch based on main.
**Success Criteria**: 2.2.0 installation guidance and changelog; 2.1.0 API baseline with no exclusions.
**Tests**: Release facts, documentation facts, dependency and action pin review.
**Status**: Complete

## Stage 2: Qualify the release
**Goal**: Verify the complete proposed release before committing or publishing.
**Success Criteria**: Pre-commit, all consumer examples, and exact 2.2.0 release rehearsal pass.
**Tests**: JDK 21 scripts/pre_commit_check.sh, scripts/verify_examples_local.sh with Scala required,
and scripts/verify_release_rehearsal.sh 2.2.0.
**Status**: Complete

## Stage 3: Land and publish from main
**Goal**: Merge the tested release inputs and publish 2.2.0 using the existing release workflow.
**Success Criteria**: Main CI and release workflow pass; v2.2.0 points to the release commit.
**Tests**: GitHub checks, public Maven Central artifacts, GitHub release assets and provenance.
**Status**: In Progress

## Stage 4: Verify published consumers and close out
**Goal**: Confirm public consumers, update post-release facts, and verify published documentation.
**Success Criteria**: Central-only consumer checks pass; main records 2.2.0 as its API baseline
and latest GA with 2.3.0-SNAPSHOT as the maintenance development line; Pages is current.
**Tests**: scripts/verify_examples_central.sh, API and documentation checks, final CI and branch state.
**Status**: Not Started

## Local qualification results

- Full pre-commit gate passed on JDK 21: all six JaCoCo counters have zero missed elements;
  mutation gate passed; compilation, API, Javadoc, documentation, pins, and SBOM checks passed.
- All local consumer examples passed with Scala/sbt and Scala/Mill required.
- Clean release rehearsal passed for exact version 2.2.0, including unchanged API checks
  against 2.1.0 with empty compatibility/evolution exclusions and seven valid release SBOMs.
- The dependency audit found no open Dependabot security alerts. The 2.2.0 Central POM and
  GitHub release did not exist before preparation.
