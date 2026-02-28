# Chance.js Java Parity Execution Plan

## Scope

- Target: **Java parity only**.
- Out of scope for this plan: Kotlin/Scala implementations.
- Source of truth for gap status: `docs/feature-parity/chancejs-parity.md`.

## Delivery Rules

- Implement in small vertical slices (one parity capability at a time).
- For each slice:
    1. Add/update tests first (parity behavior + edge cases).
    2. Implement Java core changes.
    3. Run `./scripts/pre_commit_check.sh`.
    4. Update parity doc row(s) + this plan checklist.
- Maintain current quality gates (all checks passing and coverage not regressing).

## Definition of Done (per feature)

- Chance.js-equivalent behavior covered by tests.
- Deterministic seeded behavior preserved where applicable.
- Locale-aware behavior matches existing project patterns (resource files + registry/extensibility).
- Pre-commit passes.
- Parity doc updated from `No/Partial` -> `Yes` (or justified as intentional deviation).

## Phase Plan

### Phase 0: Baseline Freeze (completed)

- [x] Freeze scope to Java parity only.
- [x] Use `chancejs-parity.md` as canonical source for remaining gaps.
- [x] Confirm current baseline quality gates are green before new slices.

### Phase 1: Core Seeding Parity (P0)

Goal: close remaining high-value seeding gaps.

- [ ] Add string seed parity (`new Chance("my-seed")` equivalent behavior).
- [ ] Add explicit re-seed API parity (reset sequence from provided seed).
- [ ] Add deterministic sequence compatibility tests across repeated runs.

Acceptance:

- Seeding rows move from `No` to `Yes` for string seed and re-seed.

### Phase 2: Options Composition Parity (P0)

Goal: close “rich options” and composability gaps.

- [ ] Introduce composable options model for generators still using fragmented flags.
- [ ] Add cross-option combination tests (multiple options applied together).
- [ ] Fill key option gaps in date/name/text APIs where Chance.js-style options exist.

Acceptance:

- `Extensive parameters` and `Option combinations` rows move to `Yes` (or reduced to documented intentional scope limits).

### Phase 3: Intentional Deviation Documentation (completed)

Goal: formalize skipped/obsolete Chance.js APIs for Java.

- [x] Mark social providers as intentional scope skip:
    - [x] `twitter()`
    - [x] `avatar({type, fileExtension, protocol, email})`
- [x] Mark `month({raw: true})` as intentional Java-native deviation.
    - Java equivalent is `java.time.Month` enum and existing numeric/name methods.
    - JS-style `{name, short_name, numeric}` object is unnecessary in this API.

Acceptance:

- Web/social rows are documented intentional deviations.
- Month raw-object row is documented as intentional deviation (Java Month enum equivalent).

### Phase 4: Optional RNG Hook + Final Hardening (P2)

Goal: finish low-priority compatibility and lock final parity quality.

- [ ] Decide and document custom RNG hook parity (`new Chance(Math.random)` equivalent) or explicit intentional deviation.
- [ ] Add regression matrix tests for all newly implemented parity APIs.
- [ ] Run full pre-commit and refresh parity summary counts.

Acceptance:

- Remaining Chance.js parity rows are either `Yes` or clearly documented intentional deviations with rationale.

## Execution Order (next steps)

1. Implement **Phase 1 / Core Seeding Parity** first.
2. Implement **Phase 2 / Options Composition** next.
3. Keep **Phase 3 / Intentional Deviation Documentation** aligned with parity doc.
4. Close with **Phase 4 / Optional RNG Hook + hardening**.

## Task Tracker

- Current step: **Phase 1 planning ready**
- Status: **Not started (implementation pending)**
- Next deliverable:
    - Implement Phase 1 features with tests.
    - Run `./scripts/pre_commit_check.sh`.
    - Update parity rows and this checklist.
