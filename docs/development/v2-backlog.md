# kRandom v2 Backlog

**Master-plan link:** [Step 1.1](v2-master-implementation-plan.md#step-11--establish-the-v2-backlog-and-change-protocol)
**Source of truth for ordering and acceptance:** [`v2-master-implementation-plan.md`](v2-master-implementation-plan.md)

This is the trackable backlog required by Step 1.1. Each work package in the master plan maps to
one row. Milestones group the release gates; labels classify the work; the decision-gate column
links each item to the D1–D10 defaults it depends on. Status mirrors the master plan checkboxes —
when they disagree, the master plan wins.

Statuses: `done` (all actions and tests checked), `partial` (some actions/tests remain, listed in
the master plan), `open` (not started), `blocked` (needs a release action or external project),
`out of scope` (ruled out by an owner decision).

## Milestone: 1.6 bridge

| Work package | Labels | Gates | Depends on | Status |
|:---|:---|:---|:---|:---|
| 1.1 Backlog and change protocol | contract, documentation | — | — | done |
| 1.2 Public API inventory | contract, compatibility | — | 1.1 | done |
| 1.3 Binary/source compatibility enforcement | compatibility, release | — | 1.2 | done |
| 1.4 1.6 deprecation bridge | contract, compatibility | D9 | 1.2, 1.3 | done |
| 1.5 Documentation facts source | documentation | — | 1.1 | done |
| 1.6 Dependency cleanup and BOM | compatibility, release | — | 1.3 | done |
| 1.7 Build and supply-chain baseline | release | — | 1.1 | done |
| 1.8 Publish and validate the 1.6 bridge | release | — | 1.3–1.7 | closed as skipped: owner decision (2026-07-11); the line moved to 2.0.0-SNAPSHOT and the 1.6 bridge exists only as the in-repo migration record |

## Milestone: v2 foundation

| Work package | Labels | Gates | Depends on | Status |
|:---|:---|:---|:---|:---|
| 2.1 Contextual generation failure model | correctness | D6 | Stage 1 | done (JPMS consumer deferred to 3.8) |
| 2.2 Recursive type model | correctness | — | 2.1 | partial: shared object/schema resolver awaits the v2 API boundary (3.1) |
| 2.3 Bean Validation normalization | correctness | — | 2.1, 2.2 | done |
| 2.4 Safe explicit Java construction | correctness, contract | D1 | 2.1–2.3 | done |
| 2.5 Immutable Kotlin support | correctness, integration | D2 | 2.2–2.4 | done |
| 2.6 Random-source contract | contract, correctness | D3 | 2.1 | done |
| 2.7 Deterministic recipes and child streams | contract | D4 | 2.6 | done |
| 2.8 Provider catalog and registry scope | contract, correctness | D8 | 2.1, 2.6 | done |
| 2.9 Financial/identity safety modes | correctness, contract | D7 | 2.7, 2.8 | done |
| 2.10 Foundation integration gate | correctness, release | — | 2.1–2.9 | done (jqwik expectation ruled out of scope 2026-07-10) |

## Milestone: v2 integrations

| Work package | Labels | Gates | Depends on | Status |
|:---|:---|:---|:---|:---|
| 3.1 Simplify the v2 API | contract, compatibility | D9 | Stage 2, 1.2–1.4 | partial: all classified removals, round-trip tests, and doc cleanup done; acronym decision, facade split, config collapse, immutability, and the new baseline remain |
| 3.2 Combinator and boundary contracts | correctness | D5 | 2.6, 3.1 | done: strict bounds, set-backed uniqueness, replay-preserving decorators, and combinator review complete |
| 3.3 Kotest replay and shrinking | integration | — | 2.5–2.7 | done: host-seeded adapters, bounded shrinking, failure recipes, and 6.1/6.2 version-range verification |
| 3.4 Typed Kotlin DSL | integration, contract | — | 2.5 | done: typed rules with validation, aligned defaults, and idiomatic configs |
| 3.5 `@KrandomTest` Spring slice | integration | — | 2.6–2.8 | done: standalone slice, core-aligned metadata defaults, full-context parity, and Spring Boot 4.1.0 published-artifact examples verified |
| 3.6 JUnit replay integration | integration | — | 2.7 | done (jqwik out of scope) |
| 3.7 Schema contract | contract, correctness | D10 | 2.2, 2.3, 2.8 | open |
| 3.8 JPMS consumers | compatibility, integration | — | 2.4 | partial: core/jackson/junit named consumers and boundary/name pinning done; the JDK matrix belongs to 3.9 CI |
| 3.9 Contract-confidence quality gates | correctness, performance | — | 2.10 | open |
| 3.10 Honest performance budgets | performance, documentation | — | 2.10, stable 3.x APIs | open |
| 3.11 Immutable resumable releases | release | — | 1.7, stable 3.x artifacts | open |
| 3.12 v2 documentation and migration | documentation | — | 3.1–3.11 | open |

## Milestone: v2 RC

| Work package | Labels | Gates | Depends on | Status |
|:---|:---|:---|:---|:---|
| 4.1 v2 alpha publication | release | — | Stage 3 | blocked: needs Maven Central publication |
| 4.2 Select and baseline two pilots | adoption | — | 4.1 | blocked: needs real consumer projects |
| 4.3 Plain-Java pilot migration | adoption | — | 4.2 | blocked |
| 4.4 Kotlin/Spring pilot migration | adoption | — | 4.2, 4.3 | blocked |
| 4.5 Pilot triage and RC1 | release, adoption | — | 4.3, 4.4 | blocked |
| 4.6 Stability soak and recovery rehearsal | release | — | 4.5 | blocked |
| 4.7 RC2 and GA decision | release | — | 4.6 | blocked |

## Milestone: v2 GA

| Work package | Labels | Gates | Depends on | Status |
|:---|:---|:---|:---|:---|
| 5.1 Publish and verify v2 GA | release | — | Stage 4 | blocked |
| 5.2 Adoption candidate inventory | adoption | — | 5.1 | blocked |
| 5.3 Canary and expansion waves | adoption | — | 5.2 | blocked |
| 5.4 Organization fixture standards | adoption, documentation | — | 5.3 | blocked |
| 5.5 Post-GA stabilization | release, adoption | — | 5.1–5.4 | blocked |

## Audit traceability with milestones

Every readiness-review P0/P1 finding maps to work packages (see the master plan's traceability
matrix) and, through the tables above, to exactly one milestone. No item in this backlog has an
undefined success criterion: each work package's "Done when" line in the master plan is its
acceptance test.

## Decision-gate notes

- D1–D10 defaults from the master plan are accepted as written; changes require a decision note
  in the master plan next to the affected step.
- Owner decision 2026-07-10: jqwik is forbidden in this project (module removed in 1.1.0); all
  jqwik-related audit expectations are out of scope.
