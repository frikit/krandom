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
**Status**: Complete

## Stage 4: Verify published consumers and close out
**Goal**: Confirm public consumers, update post-release facts, and verify published documentation.
**Success Criteria**: Central-only consumer checks pass; main records 2.2.0 as its API baseline
and latest GA with 2.3.0-SNAPSHOT as the maintenance development line; Pages is current.
**Tests**: scripts/verify_examples_central.sh, API and documentation checks, final CI and branch state.
**Status**: Complete

## Local qualification results

- Full pre-commit gate passed on JDK 21: all six JaCoCo counters have zero missed elements;
  mutation gate passed; compilation, API, Javadoc, documentation, pins, and SBOM checks passed.
- All local consumer examples passed with Scala/sbt and Scala/Mill required.
- Clean release rehearsal passed for exact version 2.2.0, including unchanged API checks
  against 2.1.0 with empty compatibility/evolution exclusions and seven valid release SBOMs.
- The dependency audit found no open Dependabot security alerts. The 2.2.0 Central POM and
  GitHub release did not exist before preparation.

## Publication results

- Release preparation PR: [#105](https://github.com/frikit/krandom/pull/105), all CI and security checks passed.
- Exact released main commit: `ee9a8521fc580cbd336bd6a9e8cf509002e5f453`.
- [Publication workflow](https://github.com/frikit/krandom/actions/runs/33891008397) passed;
  Central deployment `06a23555-8576-4b75-aceb-f83f6e9bd783` validated and published automatically.
- [GitHub release](https://github.com/frikit/krandom/releases/tag/v2.2.0) contains 33 assets;
  the release tag resolves to the exact source commit.
- Signed bundle provenance verified using `gh attestation verify --repo frikit/krandom`.
- Merged-main CI and the initial 2.2.0 Pages deployment passed.

## Public verification

- All 64 Maven Central artifact/signature files match the attested aggregation bundle byte-for-byte.
- All seven JSON SBOMs identify the expected module and version 2.2.0.
- Central-only plain Java Maven/Gradle and Kotlin/Spring Maven consumers passed, using an
  isolated Maven repository and no Maven-local dependency resolution.
- The post-release facts retain latestGaVersion=2.2.0, advance apiBaselineVersion=2.2.0,
  and set developmentVersion=2.3.0-SNAPSHOT. Example defaults match the development version.

Post-release local pre-commit checks passed against the published 2.2.0 API baseline,
including exact coverage and mutation gates. Final main CI and Pages deployment are
verified on GitHub after the closeout commit is pushed.
