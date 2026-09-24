# Mutation-testing expansion plan

## Stage 1: Define a risk-based expansion
**Goal**: Extend mutation coverage beyond schema and object generation without targeting trivial
wrappers or optimizing for an artificial 100% score.
**Success Criteria**: The first tranche covers deterministic identifiers, locale-sensitive color
and email output, safety-sensitive cryptocurrency addresses, and stateful next-word generation.
**Tests**: Review production branching, existing behavioral tests, and the current PIT survivor
report.
**Status**: Complete

## Stage 2: Measure the expanded baseline
**Goal**: Run PIT against the expanded class and test set and classify surviving mutations.
**Success Criteria**: The configured 85% mutation-score and 98% mutated-class coverage gates pass;
survivors are grouped by class and behavioral risk.
**Tests**: `JAVA_HOME=<JDK 21+> ./gradlew :core:pitest`.
**Status**: Complete

## Stage 3: Close meaningful test gaps
**Goal**: Add focused public-behavior tests for important surviving boundary, deterministic, and
safety mutations.
**Success Criteria**: New tests kill meaningful mutants without reflection-heavy assertions or
tests coupled to implementation details.
**Tests**: Targeted JUnit tests followed by the expanded PIT suite.
**Status**: Complete

## Stage 4: Verify and document the policy
**Goal**: Run the complete repository gate and record the measured scope and outcome.
**Success Criteria**: Formatting, API compatibility, tests, coverage, mutation, documentation, and
consumer checks pass; the maintained guidance explains risk-based expansion and survivor review.
**Tests**: `JAVA_HOME=<JDK 21+> ./scripts/pre_commit_check.sh` and local consumer verification.
**Status**: Complete

## Measured outcome

- The mutation target grew from 11 to 16 classes by adding `ColorGenerator`,
  `CryptoAddressGenerator`, `UUIDGenerator`, `NextWordGenerator`, and `EmailGenerator`.
- PIT generated 1,504 mutations, detected 1,389, and retained a 92% mutation score with 99%
  mutated-class line coverage. The configured 85% score and 98% coverage gates pass.
- Focused tests killed all observable new safety and determinism survivors. The two retained
  generator survivors are a duplicated zero-word boundary already rejected by the delegated
  sequence method and an internal 1,000-versus-1,001 unique-email retry boundary; neither changes
  successful public output or error type.
- Broader expansion remains incremental: add classes when their branching protects deterministic,
  validation, parsing, or safety behavior, and review survivors instead of using 100% as a target.
