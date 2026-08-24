# krandom 2.1 Hardening Plan

This plan turns the repository review into incremental, verifiable changes. It deliberately
freezes unrelated provider expansion while forward-looking object-generation controls,
performance evidence, test strength, and replacement documentation are improved.

## Stage 1: Performance Evidence

**Goal**: Separate structural and semantic object-generation costs so optimization decisions use
equivalent workloads.

**Success Criteria**: Benchmarks state equivalent semantics, use publication-grade settings, and
define documented regression budgets.

**Tests**: JMH structural/semantic comparisons and deterministic benchmark fixtures.

**Status**: Complete

## Stage 2: Object-Generation Hot Paths

**Goal**: Separate structural and semantic object-generation costs and remove avoidable hot-path
work without changing generated behavior.

**Success Criteria**: Identified metadata/reflection work is cached safely and measured workloads
improve without changing generated behavior.

**Tests**: Characterization tests, focused core tests, and JMH structural/semantic comparisons.

**Status**: Complete

## Stage 3: Object-Generation Controls

**Goal**: Add the highest-value controls needed for migrations from object fixture libraries.

**Success Criteria**: Type-safe property selection, immutable reusable models, correlated
assignments, strict configuration validation, and contextual rules are available through a
small coherent API.

**Tests**: Public behavior tests for happy paths, invalid selectors/rules, deterministic replay,
immutability, and error messages.

**Status**: Complete

## Stage 4: Internal Structure and Test Strength

**Goal**: Reduce responsibility and complexity in the largest resolver/configuration hotspots
while strengthening tests that detect behavioral regressions.

**Success Criteria**: Extracted components have single responsibilities; existing behavior tests
remain green; mutation and consumer-contract coverage protects high-risk paths and integration
modules.

**Tests**: Focused unit tests, consumer compilation tests, mutation checks, and module tests.

**Status**: Complete

## Stage 5: Replacement Documentation and Release Readiness

**Goal**: Publish precise, current replacement boundaries and verify the complete repository.

**Success Criteria**: Plans no longer contradict released artifacts; competitor guidance reflects
current upstream capabilities; supported, partial, and unsupported workflows are explicit; all
required checks pass.

**Tests**: `JAVA_HOME=<JDK 21+> ./scripts/pre_commit_check.sh`, documentation validation, and a
publication-grade benchmark run where practical.

**Status**: Complete

Verification completed with the mandatory pre-commit gate, all standalone consumer builds, and
isolated quick benchmark runs covering both the complete suite and competitor-only dashboard
formats. Publication-grade benchmark settings are encoded in the runner; quick-run measurements
remain diagnostic only.
