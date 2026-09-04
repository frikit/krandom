# Release 2.3.0: compatible fixture and replay improvements

Scope: recover library-owned profile configuration after failure, add explicit clock snapshots,
offer independent object-field streams without changing existing defaults, integrate the options
with Kotlin/JUnit/Kotest consumers, and adopt the reviewed documentation cleanup. Existing v1
recipes and default seeded output remain compatible; custom callbacks remain non-portable.

## Stage 1: Specify and reproduce
**Goal**: Turn the reviewed failures into deterministic behavior tests and define compatibility.
**Success Criteria**: Failing profile and clock tests; independent-field expectations; legacy fixtures.
**Tests**: Controlled clocks, partial/nested profile failures, seed-owned overrides and exclusions.
**Status**: Complete

## Stage 2: Implement compatible behavior
**Goal**: Deliver narrow core and integration changes with documented opt-in boundaries.
**Success Criteria**: Focused tests pass; no compatibility exclusions; old defaults unchanged.
**Tests**: Core, Kotlin, JUnit and Kotest tests; v1 recipe round trips; API reports.
**Status**: Complete

## Stage 3: Qualify and document
**Goal**: Complete documentation, consumer tests, coverage, mutation and exact release rehearsal.
**Success Criteria**: All local gates pass and release notes describe supported behavior and limits.
**Tests**: pre_commit_check.sh, verify_examples_local.sh, verify_release_rehearsal.sh 2.3.0,
old-consumer compatibility and recipe/output comparisons against 2.2.0.
**Status**: Complete

## Stage 4: Merge and publish
**Goal**: Merge reviewed release inputs to main and publish 2.3.0 through the established workflow.
**Success Criteria**: PR and main checks pass; release tag points to the exact main commit.
**Tests**: Java 21/25 CI, native-image smoke, release workflow and provenance.
**Status**: In Progress

## Stage 5: Verify public release and close out
**Goal**: Verify published artifacts and consumers, update version facts, and clean release branches.
**Success Criteria**: Central-only consumers pass; artifacts match the signed bundle; main uses
2.3.0 as latest GA/API baseline and 2.4.0-SNAPSHOT for development; Pages is current.
**Tests**: Central artifact/signature comparison, SBOM/provenance, final CI and branch state.
**Status**: Not Started

## Contract decisions

- Clock snapshots are explicit. Existing general-purpose live clocks remain live.
- Independent streams apply to seed-owned object generation. The legacy default retains its
  recipe-portability check. Arbitrary callback output and dependent semantic values are not promised
  independent of their inputs.
- The existing named-child algorithm stays unchanged. An explicit non-default policy is recorded
  as a recipe setting; absent settings retain v1 legacy semantics. Older readers reject unknown
  settings rather than silently replaying different behavior.
- Profile rollback covers configuration collections and cached generator references, not effects
  outside the faker or randomness consumed by a callback that generates objects itself.

## API gate investigation

The initial API run rejected unclassified additions. A method-only classification then left
GeneratorConfig and its Builder flagged MODIFIED; the first full pre-commit run repeated that
same classification failure. Reassessment of the XML showed both classes binary/source compatible
with no remaining changed public members. The v2.1.0 inventory uses exact class classifications
for this same private-field case. Alternatives considered were changing the japicmp gate, avoiding
configuration state, and following the existing narrow class classification. The latter preserves
the gate and accurately records the new immutable policy fields. The complete new public surface
remains the enum, three config/builder methods, and one Kotlin method; compatibility exclusions
remain empty. Recheck the unfiltered compatibility report and remove release-specific evolution
entries when the published API baseline advances.


## Qualification evidence

- Deterministic profile regressions reproduced the original failure before implementation.
- Core, JUnit engine tests, Kotlin DSL and Kotest snapshot/replay tests pass.
- Full pre-commit gate passes with zero missed JaCoCo counters and 1,229 of 1,342 critical-path
  mutations detected (92%, including one timeout).
- Every local consumer passes: Java/Kotlin Gradle and Maven, integration modules, JPMS, sbt and Mill.
- `scripts/verify_v2_consumer_compatibility.sh` compiled a consumer and extension against the
  released 2.2.0 core jar, then ran the identical bytecode against 2.3.0-SNAPSHOT. Default recipes
  (excluding library-version metadata), plain/custom/excluded fixtures, temporal sequences,
  ObjectModel and extension provider/schema output matched exactly.
- API compatibility passes against 2.2.0 with no compatibility exclusions. The unfiltered report
  confirms only the enum and documented additive methods; internal helper/state changes are
  classified separately in the evolution allowlist.

- [JMH qualification](../benchmarks/2.3.0/README.md) found default throughput within 1% and
  allocations within 0.2% of 2.2.0. The new customized independent-stream cost is documented.

- Final release-input pre-commit checks and the exact 2.3.0 release rehearsal passed, including
  API checks against 2.2.0 and all seven versioned release SBOMs.
