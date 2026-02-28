# DataFaker Java Parity Execution Plan

## Scope

- Target: **Java parity only**.
- Out of scope for this plan: Kotlin/Scala implementations.
- Source of truth for gap status: `docs/feature-parity/datafaker-parity.md`.
- Scope guard: prioritize high-value providers; do not attempt full 200+ provider clone.

## Delivery Rules

- Implement in small vertical slices (one parity capability at a time).
- For each slice:
    1. Add/update tests first (behavior + edge cases).
    2. Implement Java core changes.
    3. Run `./scripts/pre_commit_check.sh`.
    4. Update parity doc rows + this checklist.
- Keep package structure reusable and domain-oriented (no utility dumping).

## Definition of Done (per feature)

- DataFaker-equivalent behavior covered by tests.
- Seeded/deterministic behavior preserved where applicable.
- Locale behavior follows existing file-backed registry patterns.
- Pre-commit passes and coverage does not regress.
- Parity row updated to `Yes`, `Partial`, or explicit `Intentional skip`.

## Phase Plan

### Phase 0: Baseline Freeze (completed)

- [x] Freeze Java-only parity scope.
- [x] Use `datafaker-parity.md` as canonical gap source.
- [x] Keep previous completed features as-is; only close remaining high-value gaps.

### Phase 1: Core API Gaps (P0)

Goal: close cross-domain capabilities used broadly across tests and fixtures.

- [x] Implement template helpers parity:
    - [x] `numerify()`
    - [x] `letterify()`
    - [x] `bothify()`
- [x] Implement unique value wrapper parity (`faker.unique()` equivalent).
- [x] Add API-level option composition tests for new helpers.

Acceptance:

- Template and unique-enforcement rows move to `Yes`.

### Phase 2: Address & Date Foundations (P0)

Goal: cover the highest-value missing business fixture data.

- [x] Street/address components:
    - [x] `streetName`
    - [x] `streetAddress`
    - [x] `streetAddressNumber/buildingNumber`
    - [x] `secondaryAddress`
    - [x] `fullAddress`
- [x] Date range methods:
    - [x] `future()`
    - [x] `past()`
    - [x] `between()`

Acceptance:

- High-priority address/date rows move to `Yes` or justified `Partial`.

### Phase 3: Business Profile Pack (P1)

Goal: close common enterprise test-data gaps.

- [ ] Company/business generators:
    - [ ] company name/suffix
    - [ ] industry
    - [ ] company URL
- [ ] Job/profile generators:
    - [ ] field
    - [ ] seniority
    - [ ] position
- [ ] Demographics (non-sensitive core):
    - [ ] educational attainment
    - [ ] marital status

Acceptance:

- Core business/profile rows move to `Yes`.

### Phase 4: Networking & Platform IDs (P1)

Goal: close practical infrastructure-data gaps for integration tests.

- [ ] Network primitives:
    - [ ] private/public IPv4
    - [ ] IPv4/IPv6 CIDR
    - [ ] MAC address
    - [ ] port
- [ ] Web/platform helpers:
    - [ ] slug
    - [ ] user agent
- [ ] Optional identifiers:
    - [ ] UUID v7 (if kept in scope)

Acceptance:

- Medium-priority network/platform rows move to `Yes` or documented intentional deviations.

### Phase 5: Intentional Scope Decisions (P2)

Goal: explicitly close planning scope for low-ROI provider explosion.

- [ ] Mark low-value/niche providers as intentional skips with rationale:
    - [ ] entertainment/pop-culture providers
    - [ ] niche market-specific IDs not in current locale strategy
    - [ ] schema output formats (CSV/JSON/YAML/XML) unless promoted to roadmap
- [ ] Publish final parity summary with implemented vs skipped totals.

Acceptance:

- Remaining `No` rows are either active backlog or intentional skip with clear rationale.

## Execution Order (next steps)

1. Implement **Phase 3 / Business Profile Pack**.
2. Implement **Phase 4 / Networking & Platform IDs**.
3. Finalize **Phase 5 / Intentional Scope Decisions**.

## Task Tracker

- Current step: **Phase 3 planning ready**
- Status: **P0 complete (Phases 1-2 completed)**
- Next deliverable:
    - Build business profile generators (company/job/demographics core) with tests.
    - Run `./scripts/pre_commit_check.sh`.
    - Update parity rows and this checklist.
