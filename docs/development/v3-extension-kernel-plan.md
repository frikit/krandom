# v3 Extension Kernel

## Stage 1: Define the public extension contract
**Goal**: Define a small, configuration-scoped module API around the existing provider descriptor model.
**Success Criteria**: Modules have stable IDs, explicit installation, immutable resolved contributions, and deterministic registration order.
**Tests**: API validation and duplicate module/provider/schema-name rejection.
**Status**: Complete

## Stage 2: Prove metadata-complete provider contributions
**Goal**: Route module providers through provider lookup, schema lookup, safety metadata, and semantic object-field generation.
**Success Criteria**: One descriptor contributes all four capabilities without global registration or duplicate metadata.
**Tests**: Provider aliases, schema projections, safety metadata, semantic aliases, and configuration isolation.
**Status**: Complete

## Stage 3: Enrich contextual generation
**Goal**: Expose the full object path, declared type, declaration, and active configuration to contextual generators while retaining the v2 constructor and getters.
**Success Criteria**: Existing callers compile unchanged and new context is available at root, field, predicate, and model-rule override sites.
**Tests**: Legacy constructor behavior and nested object-path/type/config propagation.
**Status**: Complete

## Stage 4: Document and verify adoption readiness
**Goal**: Document module authoring, update extension/parity inventory, and pass all release gates.
**Success Criteria**: Public examples match the implementation; focused tests and `scripts/pre_commit_check.sh` pass on JDK 21+.
**Tests**: Documentation snippets compile where supported; full project verification succeeds.
**Status**: Complete
