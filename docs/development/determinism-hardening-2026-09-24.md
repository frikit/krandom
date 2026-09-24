# Determinism hardening plan

## Stage 1: Reproduce environment-sensitive output
**Goal**: Add focused regression tests for configured clocks and JVM-default locale leakage.
**Success Criteria**: Tests demonstrate UUIDv7 clock drift, Turkish case-mapping failures, and locale-dependent CSS decimals before production changes.
**Tests**: UUIDv7 timestamp extraction; isolated Turkish/German default-locale generation tests.
**Status**: Complete

## Stage 2: Apply minimal deterministic behavior fixes
**Goal**: Route time through `GeneratorConfig` and make machine-oriented normalization and formatting locale-explicit.
**Success Criteria**: The new regression tests pass without changing public APIs or ordinary English-locale seeded output.
**Tests**: Targeted identifier, locale-independence, and existing affected generator suites.
**Status**: Complete

## Stage 3: Verify the repository
**Goal**: Run the complete required quality gate and review the resulting diff.
**Success Criteria**: The full gate passes on Java 21+ and the working diff contains only intended changes.
**Tests**: `JAVA_HOME=<JDK 21+> ./scripts/pre_commit_check.sh` plus final diff/status review.
**Status**: Complete
