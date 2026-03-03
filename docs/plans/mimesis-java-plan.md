# Mimesis Java Parity Execution Plan

## Scope

- Target: **Java parity only**.
- Out of scope for this plan: Kotlin/Scala implementations.
- Source of truth for gaps: `docs/feature-parity/mimesis-parity.md`.
- Scope guard: prioritize Mimesis core workflows (schema-like bulk generation, locale-aware identity/address/internet/finance) before niche providers.

## Delivery Rules

- Implement in small vertical slices (one provider family at a time).
- For each slice:
    1. Add/update tests first (behavior + edge cases).
    2. Implement Java core changes.
    3. Run `./scripts/pre_commit_check.sh`.
    4. Update parity rows + this checklist.
- Keep package structure domain-oriented and reusable.
- Use locale-aware behavior where provider semantics depend on locale.
- Prefer file-backed datasets for large vocabularies.

## Definition of Done (per feature)

- Mimesis-equivalent behavior covered by tests.
- Seeded determinism preserved where applicable.
- Locale-aware behavior implemented where relevant.
- Pre-commit passes and coverage remains at 100%.
- Parity row updated to `Yes`, `Partial`, `No`, or `Intentional skip`.

## Phase Plan

### Phase 0: Baseline Re-Audit & Prioritization (P0)

Goal: normalize stale rows in `mimesis-parity.md` against current Java code.

- [x] Re-audit `mimesis-parity.md` against current Java implementation.
- [x] Reclassify rows to `Yes/Partial/No` with API-shape notes.
- [x] Retag missing rows as `P0`, `P1`, `P2`, or `Intentional skip candidate`.
- [x] Identify features already present under different names (alias candidates).

Acceptance:

- Mimesis parity doc is implementation-ready and up to date.

### Phase 1: Core P0 Surface (Identity + Address + Internet + Date/Text) (P0)

Goal: close highest-usage fixture features first.

- [x] Identity/person:
    - [x] Gender-aware name options and API-shape parity (`full_name(reverse)`, gendered options where relevant).
    - [x] Identifier/mask helpers and telephone mask-style APIs.
    - [x] Person demographics mapping retained in existing Java design (age/birthday/profile model), with parity aliases implemented where appropriate.
- [x] Address/location:
    - [x] Address/street/city/state/postal/country aliases and option parity.
    - [x] Country code format options (A2/A3/numeric).
    - [x] Calling code, continent and coordinate/timezone helper aliases.
- [x] Internet/network:
    - [x] URL/hostname/URI/tld/query-string alias normalization.
    - [x] Email uniqueness support and configurable domains.
    - [x] IP/mac/port/http status message/header convenience APIs.
- [x] Date/time/text:
    - [x] Datetime/timestamp/timezone convenience APIs and aliases.
    - [x] Word/sentence/text from vocabulary with option parity.

Acceptance:

- Mimesis high-frequency rows move to `Yes` or justified `Partial`.

### Phase 2: Finance + Commerce + Codes (P1)

Goal: close business-heavy parity for test fixture realism.

- [ ] Finance:
    - [ ] Credit-card convenience APIs and payload object parity.
    - [ ] CVV/expiration/network aliases.
    - [ ] Currency/price API-shape parity and locale-aware formatting.
- [ ] Commerce/product:
    - [ ] Product name/description/category/material coverage.
    - [ ] Product code helpers (ISBN/EAN/UPC and remaining high-value identifiers).
- [ ] Regulatory/enterprise IDs:
    - [ ] Add/align masked identifiers and country-specific IDs where practical.

Acceptance:

- Finance/commerce/core-code rows are `Yes/Partial` with explicit mapping notes.

### Phase 3: Schema/Field Bulk Generation Layer (P1)

Goal: implement Mimesis’ main differentiator in Java form.

- [ ] Bulk record generation API analogous to Mimesis `Schema`/`Field` flow.
- [ ] Function binding/lookup for provider method references.
- [ ] Deterministic seeded batch generation.
- [ ] Structured list/collection size controls and nested field generation.
- [ ] Validation and error reporting for invalid field mappings.

Acceptance:

- Java users can declaratively build and generate realistic record batches.

### Phase 4: Generic Provider Hub + Extensibility (P1)

Goal: improve developer ergonomics and custom provider support.

- [ ] Generic provider aggregator API with locale propagation.
- [ ] Runtime custom-provider registration hooks.
- [ ] Alias table for provider lookup compatibility.
- [ ] Coverage for provider discovery and conflict-resolution behavior.

Acceptance:

- Provider access and extension model is stable, testable, and documented.

### Phase 5: Advanced Utilities + Locale Breadth (P2)

Goal: expand parity depth where ROI is high.

- [ ] Choice/weighted selection and unique-pick semantics (where missing/different).
- [ ] Additional locale datasets for names/address/text/finance.
- [ ] Advanced datetime helpers (intervals, durations, offsets) and cryptographic/token helpers.
- [ ] Network/http convenience depth (headers/content type/user-agent options).

Acceptance:

- Locale-sensitive providers are scalable and consistent with file-backed data.

### Phase 6: Intentional Skips + Final Pass (P2)

Goal: finish parity tracking with explicit scope decisions.

- [ ] Mark low-ROI niche families as intentional skips with rationale:
    - [ ] Binary-file providers and heavy media/file-content generation.
    - [ ] Hardware/science/niche providers unless explicitly requested.
- [ ] Document non-1:1 API mappings where Java design intentionally differs.
- [ ] Publish final implemented-vs-skipped summary in `mimesis-parity.md`.

Acceptance:

- Remaining gaps are explicitly classified as backlog or intentional skip.

## Recommended Execution Order

1. Execute **Phase 0 / Baseline Re-Audit**.
2. Execute **Phase 1 / Core P0 Surface**.
3. Execute **Phase 2 / Finance + Commerce + Codes**.
4. Execute **Phase 3 / Schema/Field Bulk Generation Layer**.
5. Execute **Phase 4 / Generic Provider Hub + Extensibility**.
6. Execute **Phase 5 / Advanced Utilities + Locale Breadth**.
7. Finalize **Phase 6 / Intentional Scope Decisions + Final Pass**.

## Task Tracker

- Current step: **Phase 2 pending**
- Status: **In progress**
- Next deliverable:
    - Implement Phase 2 finance/commerce/codes API-shape parity slice.
    - Run `./scripts/pre_commit_check.sh` after each vertical slice.
