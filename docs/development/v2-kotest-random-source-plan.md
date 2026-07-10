# V2 Kotest Random-Source Plan

**Master-plan link:** Step 3.3 — Kotest replay and shrinking

## Stage 1: Characterize the host boundary

**Goal:** Establish how Kotest supplies deterministic per-case randomness and identify why the
legacy adapter cannot safely reuse a mutable `Generator` instance.

**Success Criteria:** The implementation uses Kotest's `RandomSource` for each sample and never
reseeds or shares an existing generator instance across property-test cases.

**Tests:** Host-seeded sequence replay and fresh per-sample factory tests.

**Status:** Complete

## Stage 2: Add a safe factory-based adapter

**Goal:** Add a config-factory adapter and make object arbitraries create fresh generators from a
host-derived child seed.

**Success Criteria:** Replaying the same Kotest source reproduces values, different cases do not
share a mutable kRandom generator, and non-portable caller-owned random sources fail clearly.

**Tests:** `KrandomArbTest` focused replay and rejection cases.

**Status:** Complete

## Stage 3: Preserve the migration boundary

**Goal:** Deprecate the mutable legacy bridges, document supported replay/shrinking behavior, and
record only the completed Step 3.3 work.

**Success Criteria:** Documentation directs new consumers to the factory-based API and does not
claim structural shrinking for arbitrary object fixtures.

**Tests:** Focused Kotest tests, full pre-commit gate, and local consumer examples.

**Status:** Complete
