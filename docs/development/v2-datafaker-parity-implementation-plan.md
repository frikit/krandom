# DataFaker Parity Implementation Plan

This record preserves the completed implementation plan for the DataFaker audit. It deliberately
improved kRandom's fixture ergonomics and interoperability without pursuing DataFaker's entire
long-tail vocabulary catalog.

## Stage 1: Re-baseline parity documentation

**Goal**: Correct the public DataFaker comparison and state the v2.0.0 parity contract honestly.

**Success Criteria**: Documentation identifies DataFaker's 263-provider catalog, 70 advertised
locale tags, schema-transformer support, and experimental native-image support; it no longer
claims that DataFaker lacks bulk exports or that all practical core work is complete.

**Tests**: `scripts/verify_documentation_facts.sh`, `scripts/verify_docs_site_links.sh`, and
runnable documentation examples.

**Status**: Complete

## Stage 2: Add focused fixture ergonomics

**Goal**: Close practical HTTP, constrained-password, variable-sequence, and finite-pool fixture
gaps without new runtime dependencies.

**Success Criteria**: HTTP fixtures are internally coherent; password policies guarantee required
character sets; sequences support deterministic bounded lengths and null probability; finite pools
exhaust exactly once per cycle.

**Tests**: Unit tests for validation, determinism, shape, exhaustion, and public factory access;
core coverage verification.

**Status**: Complete

## Stage 3: Add schema projections and formats

**Goal**: Allow schemas to project existing objects as well as generate new records, and provide
JSON-array, YAML, and TOML writers alongside existing streaming formats.

**Success Criteria**: Projection of records and POJOs is type-safe, output remains bounded-memory,
and all rendered formats are syntactically valid for nested values.

**Tests**: Projection, nested value, empty input, writer failure, determinism, and format-shape
tests; core coverage verification.

**Status**: Complete

## Stage 4: Add safe local data packs and University data

**Goal**: Provide a configuration-scoped, local-only data-pack mechanism with provenance checks,
then use it for a University provider.

**Success Criteria**: Packs have a versioned manifest, declared source/license, SHA-256-verified
data files, bounded reads, no network loading, and cannot leak across configurations. University
exposes name, degree, prefix, suffix, and place.

**Tests**: Manifest validation, checksum mismatch, malformed rows, isolation, seeded replay, and
provider behavior tests; core coverage verification.

**Status**: Complete

## Stage 5: Native-image readiness and release verification

**Goal**: Ship explicit experimental GraalVM support for core fixtures, document object-generation
constraints, and verify all repository gates.

**Success Criteria**: Reachability metadata is present, native-image smoke verification is
available when GraalVM is installed, unsupported reflective user types have documented
configuration guidance, and all normal quality gates pass.

**Tests**: Metadata validation, optional native-image smoke build, documentation checks, local
examples, and `scripts/pre_commit_check.sh` under Java 21.

**Status**: Complete
