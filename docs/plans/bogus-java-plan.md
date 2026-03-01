# Bogus Java Parity Execution Plan

## Scope

- Target: **Java parity only**.
- Out of scope for this plan: Kotlin/Scala implementations.
- Source of truth for gaps: `docs/feature-parity/feature-parity-bogus.md`.
- Scope guard: prioritize high-value Bogus APIs used in tests/fixtures first; keep locale-aware behavior where provider semantics depend on locale.

## Delivery Rules

- Implement in small vertical slices (one capability family at a time).
- For each slice:
    1. Add/update tests first (behavior + edge cases).
    2. Implement Java core changes.
    3. Run `./scripts/pre_commit_check.sh`.
    4. Update parity rows + this checklist.
- Keep package structure domain-oriented and reusable; avoid utility dumping.
- Prefer file-backed locale datasets where vocabulary lists become large.

## Definition of Done (per feature)

- Bogus-equivalent behavior covered by tests.
- Seeded determinism preserved where applicable.
- Locale-aware behavior implemented where provider semantics depend on language/region.
- Pre-commit passes and coverage does not regress.
- Parity rows updated to `Yes`, `Partial`, `No`, or `Intentional skip`.

## Phase Plan

### Phase 0: Baseline Re-Audit & Normalization (P0) ✅

Goal: establish a reliable, current parity baseline before implementation.

- [x] Re-audit `feature-parity-bogus.md` against current Java code (the doc is partially stale).
- [x] Mark each row with real status (`Yes/Partial/No`) and API-difference notes.
- [x] Tag each missing row with `P0`, `P1`, `P2`, or `Intentional skip candidate`.
- [x] Identify naming mismatches where parity exists but API shape differs.

Acceptance:

- Bogus parity document reflects current implementation reality.

### Phase 1: Faker<T> Core & Rule Model (P0)

Goal: close Bogus’s biggest architectural gap: fluent, rule-based object faker configuration.

- [ ] Add `Faker<T>`-style fluent builder API (Java idiomatic mapping).
- [ ] Add `ruleFor`-style property rules (single property and dependent-property variants).
- [ ] Add strict-mode equivalent (missing rules detection / validation pass).
- [ ] Add `ignore` / include filtering for object fields.
- [ ] Add configuration validation API (Bogus-like `AssertConfigurationIsValid()` equivalent).
- [ ] Add post-generation hook (`finishWith` style).

Acceptance:

- A Java user can define deterministic rule-based object generation with validation and hooks.

### Phase 2: Internet + Address + Person Completeness (P0) ✅

Goal: cover the most-used Bogus providers for app/domain fixtures.

- [x] Internet:
    - [x] `mac`, `port`, domain family (`domainName`, `domainWord`, `domainSuffix`), URL variants, protocol, user-agent parity checks.
- [x] Address:
    - [x] street components, city/state/zip/country, full address, latitude/longitude.
- [x] Person:
    - [x] full-name composites, suffix, password/avatar equivalents, job descriptor/area/type refinements.
- [x] Phone:
    - [x] format-driven phone generation equivalent to `phone_number_format`.

Acceptance:

- High-frequency Bogus identity/network/location fixtures move to `Yes` or justified `Partial`.

### Phase 3: Finance + Commerce + System/Data Providers (P1) ✅

Goal: close business-heavy provider parity for realistic data domains.

- [x] Finance:
    - [x] account/account name/transaction type, routing number, crypto address set parity decisions.
- [x] Commerce:
    - [x] product name/description, department/material/adjective/color, price helpers.
- [x] System:
    - [x] version, exception payload, platform-id style providers where in-scope.
- [x] Database provider subset:
    - [x] `column`, `type` equivalents.

Acceptance:

- Business/system provider coverage is usable for integration-test fixture generation.

### Phase 4: Locale Infrastructure Expansion (P1)

Goal: improve locale depth and maintainability toward Bogus-style broad locale behavior.

- [ ] Expand locale-aware datasets for person/address/phone/company/text.
- [ ] Normalize locale resources into file-backed structures with clear loader contracts.
- [ ] Add deterministic seeded tests across multiple locales per provider family.
- [ ] Add fallback rules for unsupported/partial locales.

Acceptance:

- Locale-sensitive providers show consistent behavior across core locale set with deterministic tests.

### Phase 5: Advanced Bogus Patterns (P2)

Goal: selectively implement high-value advanced features from Bogus fluent workflow.

- [ ] Rule sets (named profiles) parity strategy.
- [ ] Populate-existing-instance workflow (Java-safe equivalent).
- [ ] Clone/derive faker configurations.
- [ ] Cross-object/context-dependent rules improvements.

Acceptance:

- Advanced fluent workflows are available where they provide clear Java ecosystem value.

### Phase 6: Intentional Skip Decisions + Final Pass (P2)

Goal: close parity tracking with explicit scope decisions.

- [ ] Mark low-ROI providers as intentional skips with rationale.
- [ ] Document non-1:1 API mappings where Java design intentionally differs.
- [ ] Publish final implemented-vs-skipped summary in `feature-parity-bogus.md`.

Acceptance:

- Remaining gaps are explicitly classified as backlog or intentional skip.

## Recommended Execution Order

1. Execute **Phase 0 / Baseline Re-Audit**.
2. Execute **Phase 1 / Faker<T> Core & Rule Model**.
3. Execute **Phase 2 / Internet + Address + Person Completeness**.
4. Execute **Phase 3 / Finance + Commerce + System/Data**.
5. Execute **Phase 4 / Locale Infrastructure Expansion**.
6. Execute **Phase 5 / Advanced Bogus Patterns** (as needed).
7. Finalize **Phase 6 / Intentional Scope Decisions + Final Pass**.

## Task Tracker

- Current step: **Phase 1 pending**
- Status: **In progress**
- Next deliverable:
    - Implement Phase 1 fluent `Faker<T>` parity slice (`ruleFor`, strict validation, finish hooks).
    - Run `./scripts/pre_commit_check.sh` after each vertical slice.
