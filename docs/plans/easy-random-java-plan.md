# Easy Random Java Parity Execution Plan

## Scope

- Target: **Java parity only**.
- Out of scope for this plan: Kotlin/Scala implementations.
- Source of truth for gap status: `docs/feature-parity/easy-random-parity.md`.

## Delivery Rules

- Implement in small vertical slices (one parity capability at a time).
- For each slice:
    1. Add/update tests first (parity behavior + edge cases).
    2. Implement Java core changes.
    3. Run `./scripts/pre_commit_check.sh`.
    4. Update parity doc row(s) + this plan checklist.
- Maintain or improve current quality gates (all checks passing, coverage guard passing).

## Definition of Done (per feature)

- API parity behavior covered by tests.
- Deterministic seeding behavior preserved where applicable.
- No package-quality regressions (clear reusable Java packages, no utility dumping).
- Pre-commit passes.
- Parity doc updated from `No/Partial` -> `Yes` (or justified `Intentional deviation`).

## Phase Plan

### Phase 0: Baseline Freeze (completed)

- [x] Freeze Java-only scope.
- [x] Confirm current coverage/quality baseline is green.
- [x] Use existing parity doc as canonical gap source.

### Phase 1: Exclusion API Parity (P0)

Goal: match Easy Random exclusion ergonomics and behavior.

- [x] Add field exclusion annotation parity (`@Exclude` behavior verification matrix).
- [x] Add predicate-based exclusion API parity:
    - [x] `named(...)`
    - [x] `ofType(...)`
    - [x] `inClass(...)`
    - [x] `isAnnotatedWith(...)`
    - [x] modifier-based exclusion
- [x] Add predicate composition behavior tests (AND/OR/NOT).
- [x] Add type/package exclusion parity (where supported).

Acceptance:

- Exclusion section in parity doc is `Yes` or explicit intentional deviations.

### Phase 2: Declarative Randomizer Parity (P0)

Goal: support annotation-driven randomizers similar to Easy Random.

- [x] Add `@Randomizer` annotation support for fields/components.
- [x] Add `@RandomizerArgument` constructor argument support.
- [x] Define precedence tests:
    - [x] per-field override > per-type override > annotation > bean validation > built-in
- [x] Validate behavior for records and inherited fields.

Acceptance:

- Custom randomizer annotation rows move to `Yes`.

### Phase 3: Configuration Parity Gaps (P1)

Goal: close remaining parameter and behavior toggles.

- [x] `objectPoolSize` parity semantics.
- [x] `overrideDefaultInitialization` parity decision + implementation.
- [x] Ensure setter bypass behavior parity and tests.
- [x] Fill missing parameter combinations in object generation config.

Acceptance:

- Config section parity rows moved to `Yes`/documented deviations.

### Phase 4: Type Coverage Parity (P1)

Goal: close Java-type generation gaps relevant to Easy Random.

- [x] `Optional<T>` support.
- [x] `AtomicInteger` / `AtomicLong` support.
- [x] Legacy date/time support (`java.util.Date`, `java.sql.*`) if kept in scope.
- [x] Additional JSR-310 types (`OffsetDateTime`, `Year`, `YearMonth`, etc.)
- [x] Queue/collection concrete types (`ArrayDeque`, `PriorityQueue`, `TreeSet`, `TreeMap`, etc.)

Acceptance:

- Built-in randomizer/type coverage rows updated to `Yes`.

### Phase 5: Extension Model Parity (P2)

Goal: parity for advanced extension hooks.
Status: **SKIPPED by scope decision**.

- [x] Randomizer registry layering/priority model. (intentional skip)
- [x] Randomizer provider strategy hooks. (intentional skip)
- [x] Exclusion policy hook. (intentional skip)
- [x] Object factory hook. (intentional skip)
- [x] ServiceLoader auto-discovery. (intentional skip)

Acceptance:

- Advanced extension rows remain intentional `No/Partial` with rationale.

### Phase 6: Classpath Scanning Capability (P2)

Goal: abstract/interface concrete-type resolution parity.
Status: **SKIPPED by scope decision**.

- [x] Design and benchmark classpath scanning approach. (intentional skip)
- [x] Add opt-in scanning config and tests. (intentional skip)
- [x] Add performance guardrails and fallback behavior. (intentional skip)

Acceptance:

- Classpath scanning rows remain intentional `No`.

## Execution Order (next steps)

1. Keep the scoped Easy Random parity contract current as object-generation capabilities change.
2. Do not add runtime classpath scanning or ServiceLoader registry behavior unless the product scope changes.
3. Re-run the parity audit after any object generator API changes.

## Task Tracker

- Current step: **Parity maintenance**
- Status: **Completed (Phases 1-4 implemented; Phases 5-6 intentionally skipped)**
- Planned deliverable in next step:
    - Keep parity docs and tests aligned with implemented scope.
    - Address regressions discovered in normal feature development.
    - Update parity doc rows and rerun pre-commit.
